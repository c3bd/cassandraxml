package imc.disxmldb.security;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.io.util.FastByteArrayOutputStream;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.log4j.Logger;
import org.exist.util.DatabaseConfigurationException;
import org.exist.xmldb.XmldbURI;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Represents a user within the database.
 * 
 * @author Wolfgang Meier <wolfgang@exist-db.org> Modified by {Marco.Tampucci,
 *         Massimo.Martinelli} @isti.cnr.it MOdified by {xiafan68}@gmail.com
 */
public class User {

	private final static Logger LOG = Logger.getLogger(User.class);
	public final static User DEFAULT = new User("guest", null, "guest");


	private String[] groups = null;
	private String password = null;
	private String user;
	private int uid = -1;

	/**
	 * Indicates if the user belongs to the dba group, i.e. is a superuser.
	 */
	private boolean hasDbaRole = false;

	/**
	 * Create a new user with name and password
	 * 
	 * @param user
	 *            Description of the Parameter
	 * @param password
	 *            Description of the Parameter
	 */
	public User(String user, String password) {
		this.user = user;
		setPassword(password);

	}

	/**
	 * Create a new user with name
	 * 
	 * @param user
	 *            Description of the Parameter
	 */
	public User(String user) {
		this.user = user;
	}

	/**
	 * Create a new user with name, password and primary group
	 * 
	 * @param user
	 *            Description of the Parameter
	 * @param password
	 *            Description of the Parameter
	 * @param primaryGroup
	 *            Description of the Parameter
	 */
	public User(String user, String password, String primaryGroup) {
		this(user, password);
		addGroup(primaryGroup);
	}

	/**
	 * Add the user to a group
	 * 
	 * @param group
	 *            The feature to be added to the Group attribute
	 */
	public final void addGroup(String group) {
		if (groups == null) {
			groups = new String[1];
			groups[0] = group;
		} else {
			int len = groups.length;
			String[] ngroups = new String[len + 1];
			System.arraycopy(groups, 0, ngroups, 0, len);
			ngroups[len] = group;
			groups = ngroups;
		}
	}

	/**
	 * Remove the user to a group Added by {Marco.Tampucci and
	 * Massimo.Martinelli}@isti.cnr.it
	 * 
	 * @param group
	 *            The feature to be removed to the Group attribute
	 */
	public final void remGroup(String group) {
		if (groups == null) {
			groups = new String[1];
			groups[0] = "guest";
		} else {
			int len = groups.length;

			String[] rgroup = null;
			if (len > 1)
				rgroup = new String[len - 1];
			else {
				rgroup = new String[1];
				len = 1;
			}

			boolean found = false;
			for (int i = 0; i < len; i++) {
				if (!groups[i].equals(group)) {
					if (found == true)
						rgroup[i - 1] = groups[i];
					else
						rgroup[i] = groups[i];
				} else {
					found = true;
				}
			}
			if (found == true && len == 1)
				rgroup[0] = "guest";
			groups = rgroup;
		}

	}

	public final void setGroups(String[] groups) {
		this.groups = groups;
	}

	/**
	 * Get all groups this user belongs to
	 * 
	 * @return The groups value
	 */
	public final String[] getGroups() {
		return groups == null ? new String[0] : groups;
	}

	public final boolean hasDbaRole() {
		return hasDbaRole;
	}

	/**
	 * Get the user name
	 * 
	 * @return The user value
	 */
	public final String getName() {
		return user;
	}
	
	public void setName(String user) {
		this.user = user;
	}

	public final int getUID() {
		return uid;
	}

	/**
	 * Get the user's password
	 * 
	 * @return Description of the Return Value
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * Get the primary group this user belongs to
	 * 
	 * @return The primaryGroup value
	 */
	public final String getPrimaryGroup() {
		if (groups == null || groups.length == 0)
			return null;
		return groups[0];
	}

	/**
	 * Is the user a member of group?
	 * 
	 * @param group
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public final boolean hasGroup(String group) {
		if (groups == null)
			return false;
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].equals(group))
				return true;
		}
		return false;
	}

	/**
	 * Sets the password attribute of the User object
	 * 
	 * @param passwd
	 *            The new password value
	 */
	public final void setPassword(String passwd) {
		if (passwd == null) {
			this.password = null;
		} else {
			this.password = MessageDigester.md5(passwd, true);
		}
	}

	/**
	 * Sets the encoded passwod value of the User object
	 * 
	 * @param passwd
	 *            The new passwordDigest value
	 */
	public final void setEncodedPassword(String passwd) {
		this.password = (passwd == null) ? null : passwd;
	}

	public final String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("<user name=\"");
		buf.append(user);
		buf.append("\" ");
		/*buf.append("uid=\"");
		buf.append(Integer.toString(uid));
		buf.append("\"");*/
		if (password != null) {
			buf.append(" password=\"{MD5}");
			buf.append(password);
			buf.append('"');
		}
		buf.append(">");
		if (groups != null) {
			for (int i = 0; i < groups.length; i++) {
				buf.append("<group>");
				buf.append(groups[i]);
				buf.append("</group>");
			}
		}
		buf.append("</user>");
		return buf.toString();
	}

	/**
	 * Split up the validate method into two, to make it possible to
	 * authenticate users, which are not defined in the instance named "exist"
	 * without having impact on the standard functionality.
	 * 
	 * @param passwd
	 * @return true if the password was correct, false if not, or if there was a
	 *         problem.
	 */
	public final boolean validate(String passwd) {
		return true;
	}

	public final boolean validate(String passwd, XMLDBSecurityManager sm) {
		if (password == null) {
			return true;
		}
		if (passwd == null) {
			return false;
		}

		if (password != null) {
			if (MessageDigester.md5(passwd, true).equals(password)) {
				return true;
			}
		}
		return false;
	}

	public void setUID(int uid) {
		this.uid = uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		User other = (User) obj;

		if (other != null) {
			return user.equals(other.user) && password.equals(other.password);
		} else {
			return (false);
		}
	}
}
