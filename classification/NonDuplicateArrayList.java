package classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/*
 * An array list that does not allow duplicate elements.
 * Based on ArrayList and uses HashSet to efficiently check 
 * whether a newly added item is a duplicate one or not.
 * Did not finish implementing all the methods, only the ones I needed.
 */

public class NonDuplicateArrayList<E> extends ArrayList<E>{
	private static final long serialVersionUID = 1L;
	private HashSet<E> hashSet;

	public NonDuplicateArrayList(){
		super();
		hashSet = new HashSet<E>();
	}

	public NonDuplicateArrayList(Collection<? extends E> c){
		super(c);
		hashSet = new HashSet<E>(c);
	}

	public NonDuplicateArrayList(int initialCapacity){
		super(initialCapacity);
		hashSet = new HashSet<E>();
	}

	public NonDuplicateArrayList(E[] array){
		hashSet = new HashSet<E>();
		for (E i: array)
			add(i);
	}

	@Override
	public boolean add(E e){
		boolean added = hashSet.add(e);
		if (added)
			super.add(e);
		return added;
	}

	@Override
	public E remove(int index){
		E e;
		e = super.remove(index);
		hashSet.remove(e);
		return e;
	}

	@Override
	public boolean remove(Object o){
		boolean removed = super.remove(o);
		if (removed)
			hashSet.remove(o);
		return removed;
	}

	public void printHashSet(){
		System.out.println(hashSet.toString());
	}

	@Override
	public void clear(){
		super.clear();
		hashSet.clear();
	}

	public HashSet<E> getHashSet(){
		return hashSet;
	}

	// public boolean equals(NonDuplicateArrayList<E> nonDuplicateArrayList){
	// 	return hashSet.equals(nonDuplicateArrayList.getHashSet());
	// }

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o){
		if (o == null)
			return false;
		else if (o instanceof NonDuplicateArrayList)
			return hashSet.equals(((NonDuplicateArrayList<E>) o).getHashSet());
		else
			return false;
	}

	@Override
	public int hashCode(){
		return hashSet.hashCode();
	}

	public boolean containsAll(NonDuplicateArrayList<E> nonDuplicateArrayList){
		return hashSet.containsAll(nonDuplicateArrayList.getHashSet());
	}

	// maybe there's a better way to do this
	public void retainAll(NonDuplicateArrayList<E> nonDuplicateArrayList){
		HashSet<E> retainedHashSet = new HashSet<E>(hashSet);
		retainedHashSet.retainAll(nonDuplicateArrayList.getHashSet());
		clear();
		addAll(retainedHashSet);
		hashSet.addAll(retainedHashSet);
	}

	public void addAll(NonDuplicateArrayList<E> nonDuplicateArrayList){
		for (E e: nonDuplicateArrayList){
			add(e);
		}
	}

	public int[] toIntArray(){
		int[] result = new int[size()];
		for (int i = 0; i < size(); i++)
			result[i] = (Integer) get(i);
		return result;
	}
}
