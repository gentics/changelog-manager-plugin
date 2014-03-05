package com.gentics.changelogmanager;

public class ChangelogManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ChangelogManagerException(String msg) {
		super(msg);
	}

	public ChangelogManagerException(String msg, Exception e) {
		super(msg, e);
	}

}
