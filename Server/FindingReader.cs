using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Timers;

using SkyeTek.Tags;
using SkyeTek.Devices;
using SkyeTek.STPv3;
using SkyeTek.Readers;

//Namespace for all is SkyeTek
namespace SkyeTek
{
    //Class to find the reader and grant access to it
    class FindingReader
    {
        
        //all code is eing written here
        public static void sendReadRequest(STPv3Command cmmd, ushort adress, ushort block, USBDevice device)
        {

            STPv3Response responce;
            STPv3Request request;


            try
            {
                request = new STPv3Request();

                //Read serial number from reader
                request.Command = cmmd;
                request.Address = adress;
                request.Blocks = block;
                request.Issue(device);

                responce = request.GetResponse();
                if ((responce == null) || (!responce.Success))
                {
                    Console.Out.WriteLine("Unable to read serial number from reader");
                    Console.In.Read();
                    return;
                }

                Console.Out.WriteLine(String.Format("Serial Number:{0}",
                    String.Join("", Array.ConvertAll<byte, string>
                    (responce.Data, delegate(byte value)
                    {
                        return String.Format("{0:X2}", value);
                    }))));



            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.ToString());
            }

        }
        

        public static void sendDataRequest(STPv3Command cmmd,ushort adress, ushort block, USBDevice device, byte[] data){

            STPv3Response responce;
            STPv3Request request;


            try
            {
                request = new STPv3Request();

                //Read serial number from reader
                request.Command = cmmd;
                request.Address = adress;
                request.Blocks = block;
                request.Data = data;

                request.Issue(device);

                responce = request.GetResponse();
                //if ((responce == null) || (!responce.Success))
                //{
                //    Console.Out.WriteLine("No Reponce");
                //    Console.In.Read();
                //    return;
                //}

                //Console.Out.WriteLine(String.Format("Serial Number:{0}",
                //    String.Join("", Array.ConvertAll<byte, string>
                //    (responce.Data, delegate(byte value)
                //    {
                //        return String.Format("{0:X2}", value);
                //    }))));



            }
            catch (Exception ex)
            {
                Console.Out.WriteLine(ex.ToString());
            }
            
        }


        /**
         * Taken from http://pcheruku.wordpress.com/2008/10/21/sample-c-code-to-convert-hexadecimal-string-to-bytes/
         * Converst a Hex String to an Array of Bytes
         * */
        public static byte[] HexString2Bytes(string hexString)
        {
            //check for null
            if(hexString == null) return null;
            //get length
            int len = hexString.Length;
            if(len % 2 == 1) return null;
            int len_half = len/2;
            //create a byte array
            byte[] bs = new byte[len_half];
            try
            {
                //convert the hexstring to bytes
                for (int i = 0; i != len_half; i++)
                {
                    bs[i] = (byte) Int32.Parse(hexString.Substring(i *2, 2), System.Globalization.NumberStyles.HexNumber);
                }
            }
            catch(Exception ex)
            {
                Console.Out.WriteLine("Exception : " + ex.Message);
            }
            //return the byte array
            return bs;
        }

        public static void OnTimedEvent(object source, ElapsedEventArgs e)
        {
            
        }

    }


    }
