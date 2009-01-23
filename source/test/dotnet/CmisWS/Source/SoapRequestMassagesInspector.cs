using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class SoapRequestMassagesInspector: IClientMessageInspector {
        private string userName;
        private string password;

        public SoapRequestMassagesInspector(string userName, string password) {

            this.userName = userName;
            this.password = password;
        }

        object IClientMessageInspector.BeforeSendRequest(ref Message request, IClientChannel channel) {

            request.Headers.Clear();

            request.Headers.Add(SecurityMessageHeader.CreateHeader(userName, password));

            return null;
        }

        void IClientMessageInspector.AfterReceiveReply(ref Message reply, object correlationState) {
        }
    }
}
