package org.exist.xquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XPathException extends Exception {

	private int line = 0;
	private int column = 0;
	private String message = null;
	private List callStack = null;
    
	/**
	 * @param message
	 */
	public XPathException(String message) {
		super();
		this.message = message;
	}

	public XPathException(int line, int column, String message) {
		super();
		this.message = message;
		this.line = line;
        this.column = column;
	}
    
	/**
	 * @param cause
	 */
	public XPathException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public XPathException(String message, Throwable cause) {
		super(cause);
		this.message = message;
	}
	public XPathException(int line, int column, String message, Throwable cause) {
		super(cause);
		this.message = message;
        this.line = line;
        this.column = column;
	}
    
	public XPathException(int line, int column, Throwable cause) {
		super(cause);
        this.line = line;
        this.column = column;
	}

    public void setLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
    public void prependMessage(String msg) {
        message = msg + message;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
        StringBuilder buf = new StringBuilder();
		if(message == null)
			message = "";
		buf.append(message);
        if (getLine() > 0) {
			buf.append(" [at line ");
			buf.append(getLine());
			buf.append(", column ");
			buf.append(getColumn());
			buf.append("]");
		}
        if (callStack != null) {
            buf.append("\nIn call to function:\n");
            for (Iterator i = callStack.iterator(); i.hasNext(); ) {
                buf.append('\t').append(i.next());
                if (i.hasNext())
                    buf.append('\n');
            }
        }
        return buf.toString();
	}
    
    /**
     * Returns just the error message, not including
     * line numbers or the call stack.
     * 
     * @return error message
     */
    public String getDetailMessage() {
        return message;
    }
    
    public String getMessageAsHTML() {
        StringBuilder buf = new StringBuilder();
        if(message == null)
            message = "";
		message = message.replaceAll("\r?\n", "<br/>");
        buf.append("<h2>").append(message);
        if (getLine() > 0) {
            buf.append(" [at line ");
            buf.append(getLine());
            buf.append(", column ");
            buf.append(getColumn());
            buf.append("]");
        }
        buf.append("</h2>");
        if (callStack != null) {
            buf.append("<table id=\"xquerytrace\">");
            buf.append("<caption>XQuery Stack Trace</caption>");
            buf.append("</table>");
        }
        return buf.toString();
    }
    
}