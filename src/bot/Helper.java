package bot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.skype.Chat;
import com.skype.SkypeException;
import com.skype.User;

public class Helper {
	
	/**
	 * Loads a file and sends its contents to a user.
	 * @param user - The user to send it to
	 * @param file - The file to send
	 * @throws SkypeException
	 */
	public void getFullFile(User user, String file) throws SkypeException {
		try {
			user.chat().send(getFileInfo(file));
		} catch (Exception e) {
			user.chat().send("I could not find the help file.");
		}
	}

	/**
	 * Gets a user from a given chat by his name
	 * @param c - The chat to pull from
	 * @param username - The username to look for
	 * @return the User object represenation
	 * @throws SkypeException
	 */
	public User getUserByName(Chat c, String username) throws SkypeException {
		User[] members = c.getAllMembers();
		for (int i = 0; i < members.length; i++) {
			if (username.equalsIgnoreCase(members[i].getFullName()) || username.equalsIgnoreCase(members[i].toString())) {
				return members[i];
			}
		}
		return null;
	}

	/**
	 * Reads a file and returns its String representation
	 * @param filename - The path of the file
	 * @return the string representation
	 * @throws IOException
	 */
	public static String getFileInfo(String path) throws IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(path))) {
			byte[] c = new byte[is.available()];
			is.read(c);
			return new String(c);
		}
	}

	/**
	 * Reads a file and returns a list with each line being a different element
	 * @param path Where the file is located
	 * @return the list
	 */
	public static List<String> readAsList(String path) {
		List<String> file = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
			String curLine = null;
			while ((curLine = br.readLine()) != null) {
				file.add(curLine);
			}
			return file;
		} catch (IOException e) {
			System.err.println("Error reading file at " + path);
			return null;
		}
	}
}
