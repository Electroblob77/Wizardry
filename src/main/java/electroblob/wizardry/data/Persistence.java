package electroblob.wizardry.data;

/** Enum which defines the circumstances in which an {@link IVariable} persists, i.e. whether its value is carried over. */
public enum Persistence {

	NEVER(false, false),
	DIMENSION_CHANGE(false, true),
	RESPAWN(true, false),
	ALWAYS(true, true);

	private boolean persistsOnRespawn, persistsOnDimensionChange;

	Persistence(boolean persistsOnRespawn, boolean persistsOnDimensionChange){
		this.persistsOnRespawn = persistsOnRespawn;
		this.persistsOnDimensionChange = persistsOnDimensionChange;
	}

	public boolean persistsOnRespawn(){
		return persistsOnRespawn;
	}

	public boolean persistsOnDimensionChange(){
		return persistsOnDimensionChange;
	}
}
