package com.sharshar.taskservice.services;

import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * Created by lsharshar on 3/18/2018.
 */
@Service
public class NotificationService {

	@Value( "${sendgrid.api_key}" )
	private String apiKey;

	@Value( "${notification.sendFrom}" )
	private String sendFrom;

	@Value( "${notification.sendTo}" )
	private String sendTo;

	public void notifyMe(String subject, String contentString) throws Exception {
		Email from = new Email(sendFrom);
		Email to = new Email(sendTo);
		Content content = new Content("text/plain", contentString);
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(apiKey);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
