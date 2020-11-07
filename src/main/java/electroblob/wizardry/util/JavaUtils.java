package electroblob.wizardry.util;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Contains useful static methods for interacting with Java itself, rather than Minecraft. These methods used to be part
 * of {@code WizardryUtilities}.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class JavaUtils {

	/**
	 * Flattens the given nested collection. The returned collection is an unmodifiable collection of all the elements
	 * contained within all of the sub-collections of the given nested collection.
	 * @param collection A nested collection to flatten
	 * @param <E> The type of elements in the given nested collection
	 * @return The resulting flattened collection.
	 */
	public static <E> Collection<E> flatten(Collection<? extends Collection<E>> collection){
		Collection<E> result = new ArrayList<>();
		collection.forEach(result::addAll);
		return Collections.unmodifiableCollection(result);
	}

	// Neat way of getting a random element from a set, wasn't needed in the end but kept here for future reference
//	public static <E> E randomElement(Collection<E> collection, Random random){
//		if(collection.isEmpty()) throw new IndexOutOfBoundsException("The given collection must not be empty");
//		Iterator<E> iterator = collection.iterator();
//		for(int n = random.nextInt(collection.size()); n > 0; n--) iterator.next();
//		return iterator.next();
//	}

	/**
	 * Returns a list of all fields belonging to the given class and all those belonging to all of its superclasses.
	 * @param c The class to query
	 * @return The resulting {@link List} of fields
	 */
	public static List<Field> getAllFields(Class<?> c){
		List<Field> fields = new ArrayList<>(Arrays.asList(c.getDeclaredFields()));
		if(c.getSuperclass() != null) fields.addAll(getAllFields(c.getSuperclass()));
		return fields;
	}

}
