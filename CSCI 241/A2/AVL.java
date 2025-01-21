package avl;

// Author: Ben Fry-Holman
//Date: 7/18/2024
//Purpose: AVL and BST trees

public class AVL {

    public Node root;

    private int size;

    public int getSize() {
        return size;
    }

    /**
     * find w in the tree. return the node containing w or null if not found
     */
    public Node search(String w) {
        return search(root, w);
    }

    private Node search(Node n, String w) {
        if (n == null) {
            return null;
        }
        // found
        if (w.equals(n.word)) {
            return n;
        // left
        } else if (w.compareTo(n.word) < 0) {
            return search(n.left, w);
        // right
        } else {
            return search(n.right, w);
        }
    }

    /**
     * insert w into the tree as a standard BST, ignoring balance
     */
    public void bstInsert(String w) {
        if (root == null) {
            root = new Node(w);
            size = 1;
            return;
        }
        bstInsert(root, w);
    }

    /* insert w into the tree rooted at n, ignoring balance
     * pre: n is not null */
    private void bstInsert(Node n, String w) {
        //left side
        if (w.compareTo(n.word) < 0) {
            if (n.left == null) {
                n.left = new Node(w,n);
                size++;
            } else {
                bstInsert(n.left, w);
            }
        //right side
        } else if (w.compareTo(n.word) > 0) {
            if (n.right == null) {
                n.right = new Node(w, n);
                size++;
            } else {
                bstInsert(n.right, w);
            }
        }
    }

    /**
     * insert w into the tree, maintaining AVL balance precondition: the tree is AVL balanced and any prior insertions
     * have been performed by this method.
     */
    public void avlInsert(String w) {
        if (root == null) {
            root = new Node(w);
            size = 1;
        } else {
            avlInsert(root, w);
        }
    }

    /* insert w into the tree, maintaining AVL balance
     *  precondition: the tree is AVL balanced and n is not null */
    private void avlInsert(Node n, String w) {
        //left side
        if (w.compareTo(n.word) < 0) {
            if (n.left == null) {
                n.left = new Node(w,n);
                size++;
                rebalance(n);
            } else {
                avlInsert(n.left, w);
            }
        //right side
        } else if (w.compareTo(n.word) > 0) {
            if (n.right == null) {
                n.right = new Node(w, n);
                size++;
                rebalance(n);
            } else {
                avlInsert(n.right, w);
            }
        }
    }

    /**
     * do a left rotation: rotate on the edge from x to its right child. precondition: x has a non-null right child
     */
    public void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if(y.left != null) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        // root
        if (x.parent == null){
            root = y;
        // left
        } else if (x == x.parent.left) {
            x.parent.left = y;
        // right
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
        // calculate height using helper method
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
    }

    /**
     * do a right rotation: rotate on the edge from x to its left child. precondition: y has a non-null left child
     */
    public void rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;
        if (x.right != null) {
            x.right.parent = y;
        } 
        // if no parent must be root
        x.parent = y.parent;
        if (y.parent == null) {
            root = x;
        // right 
        } else if (y == y.parent.right) {
            y.parent.right = x;
        // left
        } else {
            y.parent.left = x;
        }
        x.right = y;
        y.parent = x;
        // calculate height using helper method
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
    }

    private int height(Node n) {
        if (n == null) {
            return (-1);
        } else {
            return n.height;
        }
    }


    /**
     * rebalance a node N after a potentially AVL-violoting insertion. precondition: none of n's descendants violates
     * the AVL property
     */
    public void rebalance(Node n) {
        while (n != null) {
            n.height = Math.max(height(n.left), height(n.right)) + 1;
            int balance = getBalance(n);
            //rotate left
            if (balance > 1) {
                if (getBalance(n.left) < 0) {
                    leftRotate(n.left);
                }
                rightRotate(n);
            // rotate right
            } else if (balance < -1) {
                if(getBalance(n.right) > 0) {
                    rightRotate(n.right);
                }
                leftRotate(n);
            }
            n = n.parent;
        }  
    }

    private int getBalance(Node n) {
        if(n == null){
            return 0;
        } else {
            return height(n.left) - height(n.right);
        }
    }

    /**
     * remove the word w from the tree
     */
    public void remove(String w) {
        remove(root, w);
    }

    /* remove w from the tree rooted at n */
    private void remove(Node n, String w) {
        // (enhancement TODO - do the base assignment first)
    }

    /**
     * print a sideways representation of the tree - root at left, right is up, left is down.
     */
    public void printTree() {
        printSubtree(root, 0);
    }

    private void printSubtree(Node n, int level) {
        if (n == null) {
            return;
        }
        printSubtree(n.right, level + 1);
        for (int i = 0; i < level; i++) {
            System.out.print("        ");
        }
        System.out.println(n);
        printSubtree(n.left, level + 1);
    }

    /**
     * inner class representing a node in the tree.
     */
    public class Node {
        public String word;
        public Node parent;
        public Node left;
        public Node right;
        public int height;

        /**
         * constructor: gives default values to all fields
         */
        public Node() {
        }

        /**
         * constructor: sets only word
         */
        public Node(String w) {
            word = w;
        }

        /**
         * constructor: sets word and parent fields
         */
        public Node(String w, Node p) {
            word = w;
            parent = p;
        }

        /**
         * constructor: sets all fields
         */
        public Node(String w, Node p, Node l, Node r) {
            word = w;
            parent = p;
            left = l;
            right = r;
        }

        public String toString() {
            return word + "(" + height + ")";
        }
    }
}
