package de.unidue.langtech.bachelor.meise.extra;

public class ConsoleLog {
	public void log(Object message) {
		String source = getMethodName(3);
		System.out.println(source + ": " + message);
	}
	
	public String getMethodName(final int depth)
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[depth].getMethodName();
	}
}