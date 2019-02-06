package debryan.util;

//import org.springframework.stereotype.Component;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
//@Component
public class ExceptionLogUtil {

	private static JsonUtil<GenericException> jsonUtil = new JsonUtil<GenericException>();

	private static GenericException getGenericException(Exception e, String customMessage) {
		if (null != customMessage && !customMessage.trim().isEmpty()) {
			return new GenericException(e, customMessage);
		} else {
			return new GenericException(e);
		}
	}

	public static void logException(Exception e, String customMessage) {
		//log.error(jsonUtil.convertObjectToJson(getGenericException(e, customMessage)));
		System.out.println(jsonUtil.convertObjectToJson(getGenericException(e, customMessage)));
	}

	public static String getLocalizedExceptionJson(Exception e, String customMessage) {
		return jsonUtil.convertObjectToJson(getGenericException(e, customMessage));
	}

}
