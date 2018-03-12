################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../FatReader.cpp \
../SdReader.cpp \
../WaveHCL.cpp \
../WaveUtil.cpp 

OBJS += \
./FatReader.o \
./SdReader.o \
./WaveHCL.o \
./WaveUtil.o 

CPP_DEPS += \
./FatReader.d \
./SdReader.d \
./WaveHCL.d \
./WaveUtil.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I/Applications/Arduino.app/Contents/Java/hardware/tools/avr/avr/include -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


