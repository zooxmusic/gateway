/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.st.si;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;


/**
 * Starts the Spring Context and will initialize the Spring Integration routes.
 *
 * @author vishal
 * @since 1.0
 *
 */
public final class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private Main() { }

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {


		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();
		DefaultSftpSessionFactory sftpSessionFactory = context.getBean(DefaultSftpSessionFactory.class);

		SftpSession session = sftpSessionFactory.getSession();
		final DirectChannel requestChannel = (DirectChannel) context.getBean("inboundMGetRecursive");
		//final PollableChannel replyChannel = (PollableChannel) context.getBean("output");



		try {
			String dir = "/HVAC - Files For Testing/";
			requestChannel.send(new GenericMessage<Object>(dir + "*"));
			/*if (!session.exists(sftpConfiguration.getOtherRemoteDirectory())) {
				throw new FileNotFoundException("Remote directory does not exists... Continuing");
			}*/

			rename(session, dir);

			dir = "/HPwES - Files For Testing/";
			requestChannel.send(new GenericMessage<Object>(dir + "*"));
			rename(session, dir);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		/*final DirectChannel requestChannel = (DirectChannel) context.getBean("inboundMGetRecursive");
		final PollableChannel replyChannel = (PollableChannel) context.getBean("output");
		

		String dir = "/HVAC - Files For Testing/";
		requestChannel.send(new GenericMessage<Object>(dir + "*"));
		Message<?> result = replyChannel.receive(1000);

		List<File> localFiles = (List<File>) result.getPayload();

		for (File file : localFiles) {
			System.out.println(file.getName());
		}*/

		System.exit(0);

	}

	private static void rename(SftpSession session, String dir) throws IOException {
		for (ChannelSftp.LsEntry entry : session.list(dir)) {
			if(!entry.getFilename().contains(".xml")) continue;
			System.out.println(entry.getFilename());
			String from = String.format("%s/%s", dir, entry.getFilename());
			String to = String.format("%s.done", from);
			session.rename(from, to);

		}
	}
}
