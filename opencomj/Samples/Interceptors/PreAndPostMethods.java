/*
 * PreAndPostMethods.java
 *
 * OpenCOMJ is a flexible component model for reconfigurable reflection developed at Lancaster University.
 * Copyright (C) 2005 Paul Grace
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package Samples.Interceptors;
import OpenCOM.*;
import java.util.*;
/**
 * Interceptors used in sample applications.
 * @author  Paul Grace
 * @version 1.3.2
 */
public class PreAndPostMethods {
    IOpenCOM pIOCM2;

    /** Creates a new instance of PreAndPostMethods */
    public PreAndPostMethods(IOpenCOM pIOCM) {
         pIOCM2 = pIOCM;
    }
    
    public String encode(String word, int key){
		int n=0;
		
		char[] temp = word.toCharArray();
		while(n<word.length())
		{
			temp[n]=(char) (temp[n]+(key));
			n++;

		}
		String result = new String(temp); 
		return result;
	}
    
     public int checkRules(String method, Object[] args){
        IUnknown pCalc = pIOCM2.getComponentPIUnknown("Calculator");
        IMetaInterface pMeta = (IMetaInterface) pCalc.QueryInterface("OpenCOM.IMetaInterface");
        Hashtable ppVals = new Hashtable();
        ppVals = pMeta.GetAllValues("Receptacle", "Samples.AdderComponent.IAdd");
        if(ppVals!=null){
            IUnknown pAdder = pIOCM2.getComponentPIUnknown("Adder");
            IMetaInterception pMetaIntc = (IMetaInterception) pIOCM2.QueryInterface("OpenCOM.IMetaInterception");
            IDelegator pDel = pMetaIntc.GetDelegator(pAdder, "Samples.AdderComponent.IAdd");
            for (Enumeration e = ppVals.keys() ; e.hasMoreElements() ;) {
                String ert = (String) e.nextElement();
                TypedAttribute vary = (TypedAttribute) pDel.GetAttributeValue(ert);
                int value = ((Integer) vary.Value).intValue();
                TypedAttribute rule = (TypedAttribute) ppVals.get(ert);
                int value2 = ((Integer) rule.Value).intValue();
                if (value!=value2){
                    return -1;
                }
            }
        }
          
        return 0;
    }

    // Decode undoes the encode, by returning down the ascii char set.

    public String decode(String word, int key){
		int n=0;
		
		//Change the string to an array of characters. So each character can be encrypted one at a time
		//as in classical caesar method

		char[] temp = word.toCharArray();
		while(n<word.length())
		{
			temp[n]=(char) (temp[n]-(key));
			n++;
		}
		String result = new String(temp); 
		return result;
	}
    
    public int Pre0(String method, Object[] args){
        // Take 8 off the first integer parameter to correct the addition
        IUnknown pAdder = pIOCM2.getComponentPIUnknown("Adder");
        IMetaInterception pMetaIntc = (IMetaInterception) pIOCM2.QueryInterface("OpenCOM.IMetaInterception");
        IDelegator pDel = pMetaIntc.GetDelegator(pAdder, "Samples.AdderComponent.IAdd");
        
        TypedAttribute vary = (TypedAttribute) pDel.GetAttributeValue("Variation");
        int value = ((Integer) vary.Value).intValue();
  
        Integer int1 = (Integer) args[1];   
        int val = int1.intValue();
        val=val-value;
        args[1] = new Integer(val);
        
        pDel.SetAttributeValue("Variation", "int", new Integer(0));
        return 0;
    }
    
    public int Pre1(String method, Object[] args){
       
        String encrypted = encode((String) args[0].toString(), 12);
        args[0]= encrypted;
        return 0;
    }
    
     public int Post0(String method, Object[] args){
        System.out.println(" Post Intercepting Method : "+ method);
        return 0;
    }
     
      public String Post1(String method, Object[] args){
        String decrypted = decode((String) args[0], 12);
        return decrypted;
    }
    
}
