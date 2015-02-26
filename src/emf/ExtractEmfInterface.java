package emf;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts an EMF interface from a class in the classpath (specified by the constant CLASS_FQN).
 * 
 * I created this program because I was using Eclipse's OCL infrastructure and needed the Calendar class represented in
 * EMF in order to refer to some of its methods and attributes.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ExtractEmfInterface {
	private static final String CLASS_FQN = "java.util.Calendar";

	private static final String INTERFACE_FILE_NAME = "interface.txt";

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws Exception {
		PrintWriter out = new PrintWriter(new File(INTERFACE_FILE_NAME));
		Class clazz = Class.forName(CLASS_FQN);

		// Starts the interface definition.
		out.println("public interface " + clazz.getSimpleName() + " {");

		// For each static field, define it in the interface, referencing the original.
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			// Only include public, static fields.
			int mod = field.getModifiers();
			if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
				// EMF annotation.
				out.println("\t/**\n\t * Static property " + CLASS_FQN + "." + field.getName() + "\n\t * @see java.util.Calendar#" + field.getName() + "\n\t * @model\n\t */");

				// Builds the CamelCase version of the field name.
				String fieldName = field.getName();
				StringBuilder fncc = new StringBuilder();
				int idx = fieldName.indexOf('_');
				while (idx != -1) {
					fncc.append(Character.toUpperCase(fieldName.charAt(0))).append(fieldName.substring(1, idx).toLowerCase());
					fieldName = fieldName.substring(idx + 1);
					idx = fieldName.indexOf('_');
				}
				fncc.append(Character.toUpperCase(fieldName.charAt(0))).append(fieldName.substring(1).toLowerCase());

				// Include a method for obtaining the value of the constant.
				out.println("\t" + field.getType().getSimpleName() + " get" + fncc + "();");
				out.println();
			}
		}

		// For each public method, define it in the interface.
		Method[] methods = clazz.getMethods();
		Set<Method> declaredMethods = new HashSet<Method>(Arrays.asList(clazz.getDeclaredMethods()));
		for (Method method : methods) {
			// Only define public, non-static methods. Only include final methods if declared.
			int mod = method.getModifiers();
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod) && (!Modifier.isFinal(mod) || declaredMethods.contains(method))) {
				// EMF annotation.
				out.println("\t/** @model */");

				// Build parameter list.
				StringBuilder paramDef = new StringBuilder();
				Class[] paramClasses = method.getParameterTypes();
				for (int i = 0; i < paramClasses.length; i++) {
					Class paramClass = paramClasses[i];
					paramDef.append(paramClass.getSimpleName()).append(" param").append(i);
					if (i != paramClasses.length - 1) paramDef.append(", ");
				}

				// Exceptions can be ignored for our EMF purposes.
				out.println("\t" + method.getReturnType().getSimpleName() + " " + method.getName() + "(" + paramDef + ");");
				out.println();
			}
		}

		// Close the interface definition.
		out.println("}");
		out.close();

		System.out.println("Done!");
	}
}
