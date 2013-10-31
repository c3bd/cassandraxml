/**
 * @author xiafan xiafan68@gmail.com
 */
package imc.disxmldb.gms;

/**
 * This interface defines the subject interface needed to manage a gms subject.
 */
public interface IGMSSubject {
	public void register(IGMSObserver observer);

	public void unRegister(IGMSObserver observer);

	public void GMSNotify();
}
