package debryan.util;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import lombok.Data;

@Data
public class GenericException implements Serializable {

	private static final long serialVersionUID = 1;

	private Map<Integer, ExceptionPoint> exceptionTraceHeirarchy;

	private ExceptionMeta exceptionMeta;

	public void addTraceHeirarchy(int index, StackTraceElement stackTraceElement) {
		ExceptionPoint exceptionPoint = new ExceptionPoint(stackTraceElement);
		this.exceptionTraceHeirarchy.put(index, exceptionPoint);
	}

	public void setTraceHeirarchy(StackTraceElement[] stackTraceElements) {
		this.exceptionTraceHeirarchy = new TreeMap<Integer, ExceptionPoint>();

		// Parallel
		IntStream intStream = IntStream.range(0, stackTraceElements.length).parallel();
		intStream.forEach(index -> this.addTraceHeirarchy(index, stackTraceElements[index]));

		// Sequential
		/*
		 * for (StackTraceElement stackTraceElement : stackTraceElements) {
		 * this.addFatneKiHeirarchy(stackTraceElement); }
		 */
	}

	public void setMeta(String classType, String message, Throwable cause) {
		this.exceptionMeta = new ExceptionMeta(classType, message, cause);
	}

	public GenericException() {

	}

	public GenericException(Exception e) {
		this.setTraceHeirarchy(e.getStackTrace());
		this.setMeta(e.getClass().getName(), e.getMessage(), e.getCause());
	}

	public GenericException(Exception e, String customeMessage) {
		this(e);
		this.getExceptionMeta().setCustomMessage(customeMessage);;
	}

}
