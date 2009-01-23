namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public interface CmisManipulationsStrategy<R> {
        string getName();

        R performManipulations();
    }
}
