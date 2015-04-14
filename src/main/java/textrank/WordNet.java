/*
Copyright (c) 2009, ShareThis, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the ShareThis, Inc., nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package textrank;


import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.extjwnl.data.POS;

import java.io.FileInputStream;


/**
 * Access to WordNet through JWNL.
 * @author paco@sharethis.com
 * @author flo@leibert.de
 */

public class
    WordNet
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(WordNet.class.getName());


    /**
     * Protected members.
     */

    protected static Dictionary dictionary = null;
    protected static MorphologicalProcessor mp = null;


    /**
     * Singleton
     */

    public static void
	buildDictionary (final String res_path, final String lang_code)
	throws Exception
    {


	    dictionary = Dictionary.getInstance(new FileInputStream("/Users/sang/Temp/swsd/data/file_properties.xml"));
	    mp = dictionary.getMorphologicalProcessor();
	}


    /**
     * Access the Dictionary.
     */

    public static Dictionary
	getDictionary ()
    {
	return dictionary;
    }


    /**
     * Lookup the first lemma found.
     */

    public static IndexWord
	getLemma (final POS pos, final String derivation)
	throws JWNLException
    {
        return mp.lookupBaseForm(pos, derivation);
    }
}
