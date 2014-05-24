package bot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageAdapter;
import com.skype.Skype;
import com.skype.SkypeException;

public class MessageHandle {

	private Map<Chat, PythonChat> interpMap = new HashMap<>();
	public static final int TIMEOUT = 5000;

	public static final ExecutorService exec = Executors.newFixedThreadPool(4);

	/**
	 * Initializes the message handler
	 */
	public MessageHandle() {

		try {
			Skype.addChatMessageListener(new ChatMessageAdapter() {
				@Override
				public void chatMessageReceived(ChatMessage msg) throws SkypeException {
					parseMessage(msg);
				}

				@Override
				public void chatMessageSent(ChatMessage msg) throws SkypeException {
					parseMessage(msg);
				}
			});
		} catch (SkypeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses a chat message into Python
	 * @param msg - The msg to parse
	 * @throws SkypeException
	 */
	private void parseMessage(ChatMessage msg) throws SkypeException {
		String body = msg.getContent();
		String speaker = msg.getSenderDisplayName();
		Date time = msg.getTime();
		if (new Date().getTime() - time.getTime() > 10000) { //if it's been too long since that message was sent, just ignore it. Avoids having to process thousands of messages after disconnection
			return;
		}

		PythonChat pc = interpMap.get(msg.getChat());
		if (pc == null) {
			pc = new PythonChat(msg.getChat());
			interpMap.put(msg.getChat(), pc);
		}

		if (body.startsWith("!")) {
			if (body.startsWith("!python")) {
				String[] lines = body.split("\n");
				final StringBuilder source = new StringBuilder();
				for (int i = 1; i < lines.length; i++) {
					source.append(lines[i] + "\n");
				}

				final PythonChat chat = pc;
				Future<Void> call = exec.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						chat.python.exec(source.toString());
						return null;
					}

				});

				try {
					System.out.println(msg.getId() + ": Python started at " + new Date().getTime());
					call.get(TIMEOUT, TimeUnit.MILLISECONDS);
					System.out.println(msg.getId() + ": Python ended at " + new Date().getTime());
					pc.out.print();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					pc.chat.send(e.getCause().toString());
					System.out.println(msg.getId() + ": Python error at " + new Date().getTime());
				} catch (TimeoutException e) {
					pc.chat.send("Python timed out after " + TIMEOUT + " milliseconds");
					System.out.println(msg.getId() + ": Python timed out at " + new Date().getTime());
				}
				
				pc.out.clear();
			}
		}
	}

	class PythonChat {

		public PythonInterpreter python;
		public ChatOutputStream out;
		public Chat chat;

		public PythonChat(Chat c) {
			chat = c;
			python = new PythonInterpreter(null, new PySystemState());
			out = new ChatOutputStream(c);
			python.setOut(out);
		}
	}
}
