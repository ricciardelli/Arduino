/*
 * Copyright 2014 Richard Ricciardelli
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ricciardelli.arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * 
 * Custom Arduino connector library. Based on code found on:
 * http://playground.arduino.cc/Interfacing/Java#.U0H8UnVdWlg and little help
 * from the Arduino Library version 2.0.1. Library created by Antony García
 * González, Student of Panama's Technological University and the creator of
 * panamahitek.com. You can find all the information about this library at
 * http://panamahitek.com
 * 
 * @author Richard Ricciardelli
 * @version 1.0
 * @since 1.7
 * @see SerialPort
 * @see SerialPortEventListener
 * 
 */
public class Arduino implements SerialPortEventListener {

	/**
	 * Serial port.
	 */
	private SerialPort serialPort;

	/**
	 * The port which will be used.
	 */
	private String port;

	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results code page independent.
	 */
	private BufferedReader input;

	/**
	 * The output stream to the port
	 */
	private OutputStream output;
	
	/**
	 * Milliseconds to block while waiting for port open.
	 */
	private static final int TIME_OUT = 2000;

	/**
	 * Default bits per second for COM port.
	 */
	private static final int DATA_RATE = 9600;

	/**
	 * Constructor with default communication port.
	 * 
	 * @param port
	 *            The port to be used.
	 */
	public Arduino(String port) {
		super();
		this.port = port;
		initialize();
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine = input.readLine();
				System.out.println(inputLine);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other
		// ones.
	}

	private void initialize() {
		
		// The next line is for Raspberry Pi and gets us into the while loop and
		// was suggested here:
		// http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();

		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(port)) {
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port. Are you root?");
			return;
		}

		try {

			// Open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// Set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// Open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// Add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
		Thread thread = new Thread();
		thread.start();
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	public void sendData(String data) throws IOException {
		output.write(data.getBytes());
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Arduino [serialPort=" + serialPort + ", port=" + port + ", input=" + input + ", output=" + output + "]";
	}

}
