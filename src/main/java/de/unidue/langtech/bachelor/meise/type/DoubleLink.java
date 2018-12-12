package de.unidue.langtech.bachelor.meise.type;

public class DoubleLink<T1, T2, TDesc> {
	//Class which handles the relationship between two Objects
	//Object A, Object B, Relationship Type/Description
	//A ---> B (per definition)
	private T1 objectA = null;
	private T2 objectB = null; //if objectB=0 ==> self-reference
	private TDesc sentiment = null;
	private TDesc description = null; 
	
	public DoubleLink() { }
	
	public DoubleLink(T1 objectA, T2 objectB) {
		setObjectA(objectA);
		setObjectB(objectB);
	}
	
	public DoubleLink(T1 objectA, T2 objectB, TDesc description) {
		this(objectA, objectB);
		setDescription(description);
	}
	
	public DoubleLink(T1 objectA, T2 objectB, TDesc description, TDesc sentiment) {
		this(objectA, objectB, description);
		setSentiment(sentiment);
	}

	@SuppressWarnings("unchecked")
	public void reverseObjects() {
		if(getObjectA().getClass().equals(getObjectB().getClass())) {
			T1 objectC = getObjectA();
			setObjectA((T1) getObjectB());
			setObjectB((T2) objectC);
		} else {
			System.out.println(getObjectA() + " and " + getObjectB() + " aren't compatible with each other!");
		}
	}
	
	public T1 getObjectA() {
		return objectA;
	}

	public void setObjectA(T1 objectA) {
		this.objectA = objectA;
	}

	public T2 getObjectB() {
		return objectB;
	}

	public void setObjectB(T2 objectB) {
		this.objectB = objectB;
	}

	public TDesc getSentiment() {
		return sentiment;
	}
	
	public void setSentiment(TDesc sentiment) {
		this.sentiment = sentiment;
	}
	
	public TDesc getDescription() {
		return description;
	}

	public void setDescription(TDesc description) {
		this.description = description;
	}
}
