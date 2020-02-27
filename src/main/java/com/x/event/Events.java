package com.x.event;

import java.util.Iterator;
import java.util.List;

import com.x.tools.SyncCOWList;


public class Events implements Registrator, Notifier {
	
	static class Node extends SyncCOWList<Listener<?>> {
		final int type;
		Node next;
		public Node(int type) {
			this.type = type;
		}
	}
	
	int count;
	Node[] nodes;
	
	public Events() {
		nodes = new Node[16];
	}
	
	private int indexOf(int key) {
		return key & (nodes.length - 1);
	}
	
	private Node get(int key) {
		Node node = nodes[indexOf(key)];
		while(node != null) {
			if(node.type == key) {
				return node;
			}
			node = node.next;
		}
		return null;
	}
	
	private void put(int key, Node val) {
		put0(key, val);
		
		if(count > nodes.length) {
			resize();
		}
	}

	private void put0(int key, Node val) {
		int i = indexOf(key);
		Node node = nodes[i];
		if(node == null) {
			nodes[i] = val;
		} else {
			while(node.next != null) {
				node = node.next;
			}
			node.next = val;
		}
		++ count;
	}

    private void resize() {
    	Node[] old = increase();
    	for(Node n : old) {
			while(n != null) {
				Node t = n.next;
				n.next = null;//make pure
				put0(n.type, n);
				n = t;
			}
		}
	}

	private Node[] increase() {
		Node[] old = nodes;
		nodes = new Node[(old.length << 1)];
    	count = 0;
    	return old;
	}

	@Override
    public void notify(Event event) {
        List<Listener<?>> list = get(event.type);
        if(list != null) {
            for (Listener<?> listener : list) {
                listener.onEvent0(event);
            }
        }
    }

    @Override
    public void regist(Listener<? extends Event> listener) {
        getOrNew(listener.type).add(listener);
    }

    private List<Listener<?>> getOrNew(int type) {
        List<Listener<?>> list = get(type);
        return list == null ? getOrNewSafely(type) : list;
    }

    private synchronized List<Listener<?>> getOrNewSafely(int type) {
        Node list = get(type);
        if(list == null) {
            list = new Node(type);
            put(type, list);
        }
        return list;
    }

	@Override
    public void unregist(int group) {
		for(Node n : nodes) {
			while(n != null) {
				Iterator<Listener<?>> it = n.iterator();
				while(it.hasNext()) {
	                Listener<?> next = it.next();
	                if(next.group == group) {
	                    n.remove(next);
	                }
	            }
				n = n.next;
			}
		}
    }
	
	public void unregist(Listener<?> listener) {
		Node node = get(listener.type);
		if(node != null && !node.isEmpty()) {
			node.remove(listener);
		}
	}

}
