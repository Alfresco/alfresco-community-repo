using System;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Description;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class SoapRequestHeaderProcessorAttribute: Attribute, IEndpointBehavior {
        private string userName;
        private string password;

        public SoapRequestHeaderProcessorAttribute(string userName, string password) {

            this.userName = userName;
            this.password = password;
        }

        void IEndpointBehavior.ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime) {

            clientRuntime.MessageInspectors.Add(new SoapRequestMassagesInspector(userName, password));
        }

        void IEndpointBehavior.AddBindingParameters(ServiceEndpoint endpoint,
                                                                        BindingParameterCollection bindingParameters) {
        }

        void IEndpointBehavior.ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher) {
        }

        void IEndpointBehavior.Validate(ServiceEndpoint endpoint) {
        }
    }
}
