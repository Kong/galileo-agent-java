package com.mashape.analytics.agent.connection.pool;

public class MessangerPool {
	private static final ThreadLocal<Messenger> MESSANGERPOOL = new ThreadLocal<Messenger>() {
		protected Messenger initialValue() {
			return new Messenger();
		}
	};
	
	public static Messenger get(){
		return MESSANGERPOOL.get();
	}

}
