namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public interface CmisTypesConverter<R, S> {
        R convertProperties(S sourceData);
    }
}
