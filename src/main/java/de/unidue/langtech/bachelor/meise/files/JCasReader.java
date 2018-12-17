package de.unidue.langtech.bachelor.meise.files;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class JCasReader extends ResourceCollectionReaderBase
{
    /**
     * In lenient mode, unknown types are ignored and do not cause an exception to be thrown.
     */
    public static final String PARAM_LENIENT = "lenient";
    @ConfigurationParameter(name = PARAM_LENIENT, mandatory = true, defaultValue = "true")
    private boolean lenient;

    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;
        try {
            is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
            XmiCasDeserializer.deserialize(is, aCAS, lenient);

            // Override language using PARAM_LANG if that is set
            if (getLanguage() != null) {
                aCAS.setDocumentLanguage(getLanguage());
            }
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
        finally {
            is.close();
        }
    }
}
