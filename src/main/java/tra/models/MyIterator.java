package tra.models;

import java.util.ListIterator;

public class MyIterator<T> {

        private final ListIterator<T> listIterator;
        public ListIterator<T> listIterator() {
            return listIterator;
        }

        private boolean nextWasCalled = false;
        private boolean previousWasCalled = false;

        public MyIterator(ListIterator<T> listIterator) {
            this.listIterator = listIterator;
        }

        public T next() {
            nextWasCalled = true;
            if (previousWasCalled) {
                previousWasCalled = false;
                listIterator.next ();
            }
            return listIterator.next ();
        }

        public boolean hasNext() {
            return listIterator.hasNext();
        }

        public boolean hasPrevious() {
            return listIterator.hasPrevious();
        }

        public T previous() {
            if (nextWasCalled) {
                listIterator.previous();
                nextWasCalled = false;
            }
            previousWasCalled = true;
            return listIterator.previous();
        }

    }