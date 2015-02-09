using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Timers;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.IO;

using SkyeTek;
using SkyeTek.Devices;
using SkyeTek.Readers;
using SkyeTek.STPv3;
using SkyeTek.Tags;

namespace SkyeTek
{
    class ResponceReader
    {
        //USB device
        private USBDevice usb;

        //port currenting being used for antenna
        private int port = 80;
        
        //Requests 
        private STPv3Request TagRequest;

        private const int SOCKET_PORT_1 = 6666;
        private const string host = "localhost";
        
        private TcpListener server1;
        private NetworkStream networkStream1;

        public ResponceReader()
        {
        }

        private void initializeRFID() {
            usb.Open();
            
            //read the serial number to make sure we have the reader
            returnReaderInfo(usb);
            
            //set up the request for  tag
            SetupTagRequest();

            //Send the permission to change system parameters
            sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 8, 1, usb, HexString2Bytes("80"));
            sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("81"));
            port = 81;
        }

        public void run()
        {
            while (true)
            {
                //wait for new client
                TcpClient client1 = server1.AcceptTcpClient();
                Console.Out.WriteLine("Accepted new client1");

                try
                {
                    // get stream to send on
                    networkStream1 = client1.GetStream();
                    byte[] bytesFrom1 = new byte[10025];

                }
                catch (Exception ex)
                {
                	Console.Out.WriteLine(ex.StackTrace);
                }


               ListenforCommands();

               Console.Out.WriteLine("Connection Dropped. \nWaiting for next client");
	           client1.Close();
            }
            server1.Stop();
            Console.WriteLine(" >> exit");
        }

        public void ListenforCommands()
        {
            //start at north
            string dataFromClient= "N";
            
            //loop to check for new antenna changes
            while (!dataFromClient.Equals("q")){
   
   					// Get the card on the current antenna
                    if (dataFromClient.Equals("T"))
                    {
                        //Console.Out.WriteLine("Getting a tag");
                        getTag(usb, port);
                        //Console.Out.WriteLine("Got a tag");
                    } 

                    //switch antenna
                    else 
                    {
                    	string antenna = dataFromClient.Substring(0, 1);
                        //Console.Out.WriteLine("Switching to : " + antenna);
                        switchAntenna(antenna.ToCharArray()[0]);
                        //Console.Out.WriteLine("Antenna switched");
                    }

                    //read the new command
                    try
                    {
                        //Console.Out.WriteLine("Getting command");
                        byte message = ((byte)networkStream1.ReadByte());
                        //convert byte string to string
                        byte[] messageArray = new byte[1];
                        messageArray[0] = message;
                        dataFromClient = System.Text.Encoding.ASCII.GetString(messageArray);
                        //Console.Out.WriteLine("Got : " + dataFromClient);
                    }
                    catch (IOException e)
                    {
                        Console.Out.WriteLine(e.StackTrace);
                    }
                    
                }
            }

        

        /**
         * Set the sockets
         * */
        public void setupSockets()
        {
            IPHostEntry hostentry = Dns.GetHostEntry(host);
            IPEndPoint ipe = new IPEndPoint(IPAddress.Any, SOCKET_PORT_1);
            server1 = new TcpListener(ipe);
            server1.Start();
            Console.Out.WriteLine("Server Started on port :" + SOCKET_PORT_1);
        }

        /**
         * Meathod to set up the TagRequest
         * */
        private void SetupTagRequest()
        {
            //declare new request
            TagRequest = new STPv3Request();
            //Set up tag
            Tag tag = new Tag();
            tag.Type = TagType.ISO_MIFARE_ULTRALIGHT;
            //set parameters for the request
            TagRequest.Tag = tag;
            TagRequest.Command = STPv3Commands.SELECT_TAG;
            TagRequest.Inventory = true;
        }

        /**
         * Meathod that switchs antennas when prompted
         * 
         * @param : newport will be the char that is sent from the java program.
         * */
        private void switchAntenna(char newport)
        {
            //Console.Out.WriteLine("switchAntenna called");
            switch (newport)
            {
                case 'N':
                   // Console.Out.WriteLine("North antenna selected");
                    sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("81"));
                    port = 81;
                    break;
                case 'E':
                    sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("82"));
                    port = 82;
                    break;
                case 'S':
                    sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("83"));
                    port = 83;
                    break;
                case 'W':
                    sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("84"));
                    port = 84;
                    break;
                case 'P':
                    sendDataRequest(STPv3Commands.WRITE_SYSTEM_PARAMETER, 9, 1, usb, HexString2Bytes("8C"));
                    port = 812;
                    break;
            }
            //Console.Out.WriteLine("Antenna switched");
            
        }

        /**
         * Meathod to check if there is a tag
         * 
         * @param usbdevice - the device that needs to be checked
         * */
        private void getTag(USBDevice usbdevice, int currPort)
        {
            //Console.Out.WriteLine("Checking for tags");
            STPv3Response localResponse;
            try
            {
                //issue the request

                //if there is a responce and it is a success print out a line that has the following format
                // <Tag_ID>.<port>

                TagRequest.Issue(usbdevice);
                //Console.Out.WriteLine("Request issued");
                localResponse = TagRequest.GetResponse();

                while (localResponse != null && localResponse.Success)
                    
                {
                    if (localResponse.ResponseCode == STPv3ResponseCode.SELECT_TAG_PASS)
                    {
					    String localCard;
						
					    // ID antenna
                        if (currPort == 812)
                        {
                            localCard = formatCard(localResponse, "8C");
                        }
                        
                        // Hand antenna
                        else
                        {
                            localCard = formatCard(localResponse, Convert.ToString(currPort));
                        }
                        
                        //Console.Out.WriteLine("Sending : " + localCard);
                        sendCard (localCard);
                        return;
                    }

                    Thread.Sleep(50);
                    localResponse = TagRequest.GetResponse();
                }

                //Console.Out.WriteLine("No card");
                sendCard("NOCARD");
                        
            }

            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.StackTrace);
            }
        }

        private string formatCard(STPv3Response rfidResponse, string antennaId)
        {
            return String.Format("{0}.{1}",
                                    String.Join("", Array.ConvertAll<byte, string>(rfidResponse.TID,
                                    delegate(byte value) { return String.Format("{0:X2}", value); })),
                                    antennaId);
        }
        
        private void sendCard (string card) {
            byte[] byteCard = Encoding.ASCII.GetBytes(card);
            networkStream1.Write(byteCard, 0, byteCard.Length);
            networkStream1.Flush();
        }

        /**
         * Finds the reader on the computer
         * */
        private Boolean findReader()
        {
            //declare the device array that could be connected to the computer 
            //set it to the USBDeviceFactory.Enumerate
            Device[] device = USBDeviceFactory.Enumerate();

            //if there are no devices print responce to screen
            //return false
            if (device.Length == 0)
            {
                Console.Out.WriteLine("No USB Devices Found");
                return false;
            }
            //if not print found usb and set it to usb
            //return true
            Console.Out.WriteLine("Found USB Reader");
            usb = (USBDevice)device[0];
            return true;

        }

        /**
         * Asks for data 
         * 
         * NOTE : currently all this meathod is used for is finding the serial number
         * @param cmmd - the command to be issues
         * @param adress - the address of the memory
         * @param block - the blocks of the memory
         * @param device - the device to be read
         * */
        private void sendReadRequest(STPv3Command cmmd, ushort adress, ushort block, USBDevice device)
        {
            //new request
            STPv3Request request = new STPv3Request();

            try
            {

                //set param of the request
                request.Command = cmmd;
                request.Address = adress;
                request.Blocks = block;
                //issue request
                request.Issue(device);

                //get response
                STPv3Response response = request.GetResponse();
                //if it fails report and exit
                if ((response == null) || (!response.Success))
                {
                    Console.Out.WriteLine("Unable to read serial number from reader");
                    Console.In.Read();
                    return;
                }
                //else print out the data
                Console.Out.WriteLine(String.Format("Serial Number:{0}",
                    String.Join("", Array.ConvertAll<byte, string>
                    (response.Data, delegate(byte value)
                    {
                        return String.Format("{0:X2}", value);
                    }))));
            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.StackTrace);
            }
        }

        /**
         * Sends a request with data attached to it
         * 
         * @param cmmd - command to be used
         * @param adress - the adress of the memory
         * @param block - the blocks of memory
         * @param device - the device
         * @param data - the data to be sent
         * */
        private void sendDataRequest(STPv3Command cmmd, ushort adress, ushort block, USBDevice device, byte[] data)
        {
            //Console.Out.WriteLine("sendDataRequest called");
            try
            {
                //set parameters
                STPv3Request SystemRequest = new STPv3Request();
                SystemRequest.Command = cmmd;
                SystemRequest.Address = adress;
                SystemRequest.Blocks = block;
                SystemRequest.Data = data;
                //issue the command

        		STPv3Response response;
                SystemRequest.Issue(device);
                //Console.Out.WriteLine("Command sent");

                while (true)
                {
                    response = SystemRequest.GetResponse();
                    //Console.Out.WriteLine("Got response " + response);

                    if (response != null && response.ResponseCode == STPv3ResponseCode.WRITE_SYSTEM_PARAMETER_PASS)
                    {
                        break;
                    }

                    Thread.Sleep(50);
                }
            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.StackTrace);
            }
            //Console.Out.WriteLine("sendDataRequest returning");
        }

        /**
         * Taken from http://pcheruku.wordpress.com/2008/10/21/sample-c-code-to-convert-hexadecimal-string-to-bytes/
         * Converst a Hex String to an Array of Bytes
         * */
        private byte[] HexString2Bytes(string hexString)
        {
            //check for null
            if (hexString == null) return null;
            //get length
            int len = hexString.Length;
            if (len % 2 == 1) return null;
            int len_half = len / 2;
            //create a byte array
            byte[] bs = new byte[len_half];
            try
            {
                //convert the hexstring to bytes
                for (int i = 0; i != len_half; i++)
                {
                    bs[i] = (byte)Int32.Parse(hexString.Substring(i * 2, 2), System.Globalization.NumberStyles.HexNumber);
                }
            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.StackTrace);
            }
            //return the byte array
            return bs;
        }

        /**
         * Current returns the seiral number of the device
         * 
         * @param usbDevice - the device to be read from
         * */
        private void returnReaderInfo(USBDevice usbDevice)
        {
            //request serial number
            sendReadRequest(STPv3Commands.READ_SYSTEM_PARAMETER, 0, 4, usbDevice);
        }

        /**
         * Main Meathod
         * Currently only declare a new RunningReader class
         * */
        public static void Main()
        {
            ResponceReader read = new ResponceReader();
            if (!read.findReader())
            {
                return;
            }

            read.initializeRFID();
            read.setupSockets();
            read.run();
        }
    }
}