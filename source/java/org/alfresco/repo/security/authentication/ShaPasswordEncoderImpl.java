package org.alfresco.repo.security.authentication;

/**
 * <p>
 * SHA implementation of PasswordEncoder.
 * </p>
 * <p>
 * If a <code>null</code> password is presented, it will be treated as an empty
 * <code>String</code> ("") password.
 * </p>
 * <p>
 * As SHA is a one-way hash, the salt can contain any characters. The default
 * strength for the SHA encoding is SHA-1. If you wish to use higher strengths
 * use the argumented constructor. {@link #ShaPasswordEncoderImpl(int)}
 * </p>
 * <p>
 * The applicationContext example...
 * 
 * <pre>
 * &lt;bean id="passwordEncoder" class="org.springframework.security.authentication.encoding.ShaPasswordEncoder"&gt;
 *     &lt;constructor-arg value="256"/>
 * &lt;/bean&gt;
 * </pre>
 */
public class ShaPasswordEncoderImpl extends MessageDigestPasswordEncoder
{

    /**
     * Initializes the ShaPasswordEncoder for SHA-1 strength
     */
    public ShaPasswordEncoderImpl()
    {
        this(1);
    }

    /**
     * Initialize the ShaPasswordEncoder with a given SHA stength as supported
     * by the JVM EX:
     * <code>ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);</code>
     * initializes with SHA-256
     * 
     * @param strength
     *            EX: 1, 256, 384, 512
     */
    public ShaPasswordEncoderImpl(int strength)
    {
        super("SHA-" + strength);
    }
}