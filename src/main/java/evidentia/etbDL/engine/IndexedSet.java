package evidentia.etbDL.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 => subclass of 'set' that can quickly access a subset of its elements through an index
 => quickly retrieves facts with a specific predicate name
 => <E> type of elements that will be stored in the set (must implement {Indexable})
 => <I> type of the index
 */
public class IndexedSet<E extends Indexable<I>, I> implements Set<E> {

	private Set<E> contents;
	private Map<I, Set<E>> index;
	
    //default constructor
	public IndexedSet() {
		contents = new HashSet<>();
        index = new HashMap<I, Set<E>>();
    }
	
	//creates the set using a different collection to construct
	public IndexedSet(Collection<E> elements) {
		contents = new HashSet<>(elements);
		reindex();
	}
	
    //re-arranges all objects in 'contents'
    private void reindex() {
        index = new HashMap<I, Set<E>>();
        for (E element : contents) {
            //get all elements whose that index
            Set<E> elements = index.get(element.index());
            if (elements == null) {
                //if no entry for the index in the map
                elements = new HashSet<E>();
                //preparing an empty entry with the given index
                index.put(element.index(), elements);
            }
            elements.add(element);
        }
    }

    //retrieves the subset of the elements in the set with the specified index.
	public Set<E> getIndexed(I key) {
		Set<E> elements = index.get(key);
		if(elements == null)
            return Collections.emptySet();
		return elements;
	}

	public Collection<I> getIndexes() {
		return index.keySet();
	}
	
	@Override
	public boolean add(E element) {
		if (contents.add(element)) {
            //tries to add the element contents
			Set<E> elements = index.get(element.index());
			if (elements == null) {// no entry for index of the object
				elements = new HashSet<E>();
                // creating an entry with the index of the object
				index.put(element.index(), elements);
			}
			elements.add(element);
			return true;
		}
        //adding to contents not successful - already existing
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> elements) {
		boolean result = false;
		for(E element : elements) {
			if(add(element))
                result = true;
		}
        //true if at least one element is added
		return result;
	}

	@Override
	public void clear() {
		contents.clear();
		index.clear();
	}

	@Override
	public boolean contains(Object o) {
		return contents.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return contents.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return contents.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return contents.iterator();
	}

	@Override
	public boolean remove(Object o) {//NOT NEEDED
		if(contents.remove(o)) {
			reindex();
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = contents.removeAll(c);
		if (changed) {
			reindex();
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = contents.retainAll(c);
		if (changed) {
			reindex();
		}
		return changed;
	}

	@Override
	public int size() {
		return contents.size();
	}

	@Override
	public Object[] toArray() {
		return contents.toArray();
	}

	@Override
	public <A> A[] toArray(A[] a) {
		return contents.toArray(a);
	}	
}
