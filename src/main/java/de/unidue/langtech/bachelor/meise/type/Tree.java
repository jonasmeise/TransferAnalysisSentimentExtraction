package de.unidue.langtech.bachelor.meise.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import webanno.custom.AspectRating;

public class Tree<T> {

	//Simple Baumstruktur zum Parsen und Darstellen von Dependencies
	
    T data;
    Tree<T> parent;
    List<Tree<T>> children;
    String parentDependencyType;
    
    public String getParentDependencyType() {
		return parentDependencyType;
	}

	public void setParentDependencyType(String parentDependencyType) {
		this.parentDependencyType = parentDependencyType;
	}

	public Tree() {
    	this.children = new LinkedList<Tree<T>>();
    	parentDependencyType = "";
    }
    
    public Tree(T data) {
    	this();
        setData(data);
    }

    public Tree<T> addChild(T child) {
        Tree<T> childNode = new Tree<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Tree<T> getParent() {
		return parent;
	}

	public void setParent(Tree<T> parent) {
		this.parent = parent;
	}

	public List<Tree<T>> getChildren() {
		return children;
	}
	
	public Tree<T> addInTree(T element, Tree<T> parent){
		if(getParent()==parent.getData()) {
			return addChild(element);
		}
		else
		{
			for(Tree<T> kandidaten : getChildren()) {
				addInTree(element, parent);
			}
		}
		
		return null;
	}
	
	public ArrayList<T> getAllObjects(){
		ArrayList<T> returnList = new ArrayList<T>();
		
		returnList.add(data);
		
		for(Tree child : getChildren()) {
			returnList.addAll(child.getAllObjects());
		}
		
		return returnList;
	}
	
	public String printTree(int depth) {
		String returnwert;
		returnwert = ((Token) getData()).getCoveredText();

		if(getData() != null) {
			for(Tree<T> element : getChildren()) {
				returnwert = returnwert + "\n";
				
				for(int i=0;i<=depth;i++) {
					returnwert = returnwert + "  ";
				}
				
				returnwert = returnwert + "- " + element.printTree(depth+1);
			}
			
			return returnwert;
		}
		else
		{
			return null;
		}
	}
	
	public void generateTreeOfDependency(Collection<Dependency> dpList, Token currentRoot) {
		for(Dependency dependency : dpList) {
			if((dependency.getGovernor() == currentRoot) && (dependency.getDependent() != currentRoot)) {
				Tree<T> newChild = addChild((T) dependency.getDependent());
				newChild.setParentDependencyType(dependency.getDependencyType());
				newChild.generateTreeOfDependency(dpList, dependency.getDependent());
			}
		}
	}
	
	public boolean isElement(String element, String[] elementArray) {
		for(String e : elementArray) {
			if(e.compareTo(element) == 0) {
				return true;
			}
		}
		return false;
	}
	
	public Tree<AspectRating> findToken(AspectRating token, Collection<Tree<AspectRating>> treeList) {
		Collection<Tree<AspectRating>> newTreeList = new ArrayList<Tree<AspectRating>>();
		
		for(Tree<AspectRating> treeToken : treeList) {
			if(!treeToken.data.equals(token)) {
				if(treeToken.getChildren().size() > 0) {
					for(Tree<AspectRating> child : treeToken.getChildren()) {
						newTreeList.add(child);
					}
				}
			} else {
				return treeToken;
			}
		}
		
		if(newTreeList.size() > 0) {
			return findToken(token, newTreeList);
		} else {
			return null;
		}
	}
	
	public Tree<Token> findToken(Token token, Collection<Tree<Token>> treeList) {
		Collection<Tree<Token>> newTreeList = new ArrayList<Tree<Token>>();
		
		for(Tree<Token> treeToken : treeList) {
			if(!treeToken.data.equals(token)) {
				if(treeToken.getChildren().size() > 0) {
					for(Tree<Token> child : treeToken.getChildren()) {
						newTreeList.add(child);
					}
				}
			} else {
				return treeToken;
			}
		}
		
		if(newTreeList.size() > 0) {
			return findToken(token, newTreeList);
		} else {
			return null;
		}
	}
	
	public int tokenDistanceInTree(Token t1, Token t2) {
		Tree<Token> tree1;
		Tree<Token> tree2;
		int treeDepth1, treeDepth2, distance=0;
		boolean found = false;
		
		Collection<Tree<Token>> treeCollection1 = new ArrayList<Tree<Token>>();
		Collection<Tree<Token>> treeCollection2 = new ArrayList<Tree<Token>>();
		treeCollection1.add((Tree<Token>) this);
		treeCollection2.add((Tree<Token>) this);
		
		tree1 = findToken(t1, treeCollection1);
		tree2 = findToken(t2, treeCollection2);
		
		if(tree1!=null && tree2!=null) {
			if(!t1.equals(t2)) {
				//System.out.println(tree1 + " " + tree2);
				while(!found) {	
					treeDepth1 = tree1.getDepth();
					treeDepth2 = tree2.getDepth();
					
					//System.out.println(treeDepth1 + "  " + treeDepth2);

					if(tree1 == tree2) {
						found=true;
					}
					if(treeDepth1 == treeDepth2 && treeDepth1 == 0) {
						break;
					}
					
					if(treeDepth1 >= treeDepth2) {
						tree1 = tree1.getParent();
					} else if (treeDepth1 < treeDepth2) {
						tree2 = tree2.getParent();
					}
					
					distance++;
				}
				
				if(found) {
					return distance;
				}
			} else {
				return 0;
			}
		}
		
		return -1;
	}
	
	public int getDepth() {
		if(parentDependencyType=="ROOT") {
			return 0;
		} else {
			return parent.getDepth()+1;
		}
	}
	
	//searchDirection: false->down the tree		true->up the tree
	public boolean getRelevantInformation(LinkedList<String[]> searchFilter, LinkedList<LinkedList<T>> output, LinkedList<T> currentTokenList, int filterDepth, boolean searchDirection) {
		if (searchFilter.size() > filterDepth) {
			String[] currentInformation = searchFilter.get(filterDepth);
			Token myData = (Token) getData();
			
			if(currentTokenList.isEmpty()) {// ???????????????????????
				if(getChildren().size() > 0) {
					for(Tree<T> child : getChildren()) {
						child.getRelevantInformation(searchFilter, output, currentTokenList, filterDepth, true);
					}
				}	
				
				if(isElement(myData.getPos().getPosValue(), currentInformation) || isElement(getParentDependencyType(), currentInformation)) {
					LinkedList<T> linkedData = new LinkedList<T>();
					linkedData.add((T) myData);
					
					boolean nextInfo = true;
					while(nextInfo) {
						boolean newDirection = true;
						
						if(getParentDependencyType().compareTo("ROOT") == 0) {
							newDirection = !newDirection;
						}
						
						nextInfo = getRelevantInformation(searchFilter, output, linkedData, ++filterDepth, newDirection);
					}
				
					output.add(linkedData);
				}

				return false; //Gibt Standardrueckgabewert zurueck, nicht relevant
			}
			else
			{
				//Geschwister durchsuchen
				if(getParent()!=null && searchDirection) {
					Tree<T> myParent = getParent();
					Token parentData = (Token) myParent.getData();
					
					if(isElement(parentData.getPos().getPosValue(), currentInformation) || isElement(parent.getParentDependencyType(), currentInformation)) {
						//Jetziges Parent-Element ist gesucht?
						currentTokenList.add((T) parentData);
						return true;
					}
					
					for(Tree<T> child : myParent.getChildren()) {
						Token childData = (Token) child.getData();
						
						if(child!=this) {
							if(isElement(childData.getPos().getPosValue(), currentInformation) || isElement(child.getParentDependencyType(), currentInformation)) {
								currentTokenList.add((T) childData);
								return true; //Bezugsobjekt #1 in Kinder gefunden
							}
						}
					}
					
					return myParent.getRelevantInformation(searchFilter, output, currentTokenList, filterDepth, true); //rekursiv die Geschwister der Eltern ebenfalls durchsuchen
				}
				else {
					//aktuelles Element gesucht?
					Token currentData = (Token) getData();
					
					if(isElement(currentData.getPos().getPosValue(), currentInformation) || isElement(getParentDependencyType(), currentInformation)) {
						currentTokenList.add((T) myData);
						return true;
					}
					
					if(getChildren().size() > 0) {
						for(Tree<T> child : getChildren()) {
							return child.getRelevantInformation(searchFilter, output, currentTokenList, filterDepth, false);
						}
					}
				}
				
				return false; //absolut nichts gefunden, weder in allen Kindern noch aktuellem Tree
			}
		}
		else
		{
			return false; //Leeres Element 
		}
	}
}
