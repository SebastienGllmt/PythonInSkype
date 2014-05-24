package bot;

import java.io.IOException;
import java.io.Writer;

import com.skype.Chat;
import com.skype.SkypeException;

public class ChatOutputStream extends Writer {

	private Chat chat;
	private StringBuffer mainBuffer = new StringBuffer();

	/**
	 * Constructs an output stream to return results to a Skype chat
	 * @param c - The chat to print to
	 */
	public ChatOutputStream(Chat c) {
		super();
		chat = c;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		StringBuffer sb = new StringBuffer();
		int i;
		for (i = off; i < len; i++) {
			sb.append(cbuf[i]);
		}

		if (sb.length() != 0)
			mainBuffer.append(sb.toString());
	}

	public void print() {
		if (mainBuffer.length() == 0)
			return;

		try {
			chat.send(mainBuffer.toString());
			mainBuffer = new StringBuffer();
		} catch (SkypeException e) {
			e.printStackTrace();
		}
	}
	
	public void clear(){
		mainBuffer = new StringBuffer();
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

}
