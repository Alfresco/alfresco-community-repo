using System;
using System.Xml;
using System.ServiceModel.Channels;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    public class SecurityMessageHeader: MessageHeader {
        private const int MINIMAL_ALLOWED_STRING_LENGTH = 1;
        private const int TIME_TOKEN_POSITION = 3;
        private const int LAST_DATE_TOKEN_POSITION = 2;

        private const int EXPIRE_ODDS = 4167;

        private const string DOT = ".";
        private const char DOT_SYMBOL = '.';
        private const string MINUS = "-";
        private const string BREAK = " ";
        private const char BREAK_SYMBOL = ' ';
        private const string TIME_SPECIFICATOR = "T";
        private const string DATE_TIME_ZONE_SPECIFICATOR = "Z";

        private const string SECURITY_HEADER_NAME = "Security";
        private const string SECURITY_HEADER_NAME_SPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-w" +
                                                          "ssecurity-secext-1.0.xsd";
        private const string SECURITY_HEADER_ENTRY = "\n      <wsu:Timestamp wsu:Id=\"Timestamp-{0}\" xmlns:wsu=\"ht" +
                              "tp://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                              "        <wsu:Created>{1}</wsu:Created>\n        <wsu:Expires>{2}</wsu:Expires>\n" +
                              "      </wsu:Timestamp>\n      <UsernameToken wsu:Id=\"UsernameToken-{3}\" xmlns:wsu=" +
                              "\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" +
                              "\">\n        <Username>{4}</Username>\n        <Password Type=\"http://docs.oasis-ope" +
                              "n.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">{5}</Pas" +
                              "sword>\n        <Nonce>IGRKG/ABNywDNZ1/lxxarA==</Nonce>\n        <wsu:Created>{6}</ws" +
                              "u:Created>\n      </UsernameToken>\n";

        private string value;

        public static MessageHeader CreateHeader(string userName, string password) {

            string formattedSecurityHeader = string.Format(SECURITY_HEADER_ENTRY, new object[] {
                 DateTime.Now.TimeOfDay.Ticks, formatDate(), formatDate(new DateTime(DateTime.Now.Ticks + EXPIRE_ODDS).
                           ToLocalTime().ToString()), DateTime.Now.TimeOfDay.Ticks, userName, password, formatDate()});

            return new SecurityMessageHeader(formattedSecurityHeader);
        }

        protected override void OnWriteHeaderContents(XmlDictionaryWriter writer, MessageVersion messageVersion) {

            writer.WriteRaw(value);
        }

        public override string Actor {

            get {
                return string.Empty;
            }
        }

        public override bool IsReferenceParameter {

            get {
                return true;
            }
        }

        public override bool MustUnderstand {

            get {
                return true;
            }
        }

        public override bool Relay {

            get {
                return false;
            }
        }

        public override string ToString() {

            return base.ToString();
        }

        public override string Namespace {

            get {
                return SECURITY_HEADER_NAME_SPACE;
            }
        }

        public override string Name {

            get {
                return SECURITY_HEADER_NAME;
            }
        }

        private SecurityMessageHeader(string value) {

            this.value = value;
        }

        private static string formatDate() {

            return formatDate(DateTime.Now.ToLocalTime().ToString());
        }

        private static string formatDate(string sourceDate) {

            if (!(sourceDate is string) || (sourceDate.Length < MINIMAL_ALLOWED_STRING_LENGTH)) {
                sourceDate = DateTime.Now.ToLocalTime().ToString();
            }

            return reverseDate(sourceDate) + DOT + DateTime.Now.Millisecond + DATE_TIME_ZONE_SPECIFICATOR;
        }

        private static string reverseDate(string sample) {

            string[] dateFields = sample.Split(new char[] {DOT_SYMBOL, BREAK_SYMBOL});

            int i = LAST_DATE_TOKEN_POSITION;

            return dateFields[i--] + MINUS + dateFields[i--] + MINUS + dateFields[i] + TIME_SPECIFICATOR +
                                                                                       dateFields[TIME_TOKEN_POSITION];
        }
    }
}
