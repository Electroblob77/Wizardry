package electroblob.wizardry.util;

/**
 * A very simple container for a single object of a given type {@code T} (referred to as the <i>contents</i>). Allows
 * fixed-size collections to be made using their immutable (or unmodifiable) counterparts. This is particularly useful
 * for maps with a fixed set of keys but whose values may be changed.
 */
public class Box<T> {

	private T contents;

	public Box(T contents){
		this.contents = contents;
	}

	/** Returns the contents of this box. */
	public T get(){
		return contents;
	}

	/** Sets the contents of this box to the given object. */
	public void set(T contents){
		this.contents = contents;
	}

	// Probably misleading since methods like this generally imply immutability, which is exactly what we're not doing

//	/** Creates a new box with the given contents. This is just a static factory method that calls the constructor. */
//	public static <T> Box<T> of(T contents){
//		return new Box<>(contents);
//	}

}
