import java.io.IOException;
import java.util.LinkedList;

interface ChooseAction {
	void chooseSimple(boolean split) throws IOException, KeyException;
	void chooseComplex(LinkedList<Integer> set,boolean split) throws IOException, KeyException;
}