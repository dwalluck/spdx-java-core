/**
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2024 Source Auditor Inc.
 */
package org.spdx.core;

import java.util.Map;

import javax.annotation.Nullable;
import org.spdx.storage.IModelStore;

/**
 * @author Gary O'Neall
 * 
 * Implementation classes of this interface helps facilitate copying objects from one model to another.
 * 
 * In addition to the copy functions (methods), these objects keeps track of 
 * what was copied where so that the same object is not copied twice.
 * 
 * These objects can be passed into the constructor for ModelObjects to allow the objects to be copied.
 * 
 *
 */
public interface IModelCopyManager {
	
	   /**
		 * Copy an item from one Model Object Store to another using the source ID for the target unless it is anonymous
		 * @param toStore Model Store to copy to
		 * @param fromStore Model Store containing the source item
		 * @param sourceUri URI for the Source object
		 * @param type Type to copy
		 * @param excludeLicenseDetails If true, don't copy over properties of the listed licenses
		 * @param externalMap map of URI's to ExternalMaps for any external elements
		 * @param fromNamespace optional namespace of the from property
		 * @param toNamespace optional namespace of the to property
		 * @param fromCollectionNamespace Optional namespace for the from collection to use when creating items for collections.  Defaults to toNamespace
		 * @param toCollectionNamespace Optional namespace for the to collection to use when creating items for collections.  Defaults to toNamespace
		 * @return Object URI for the copied object
		 * @throws InvalidSPDXAnalysisException
		 */
		public TypedValue copy(IModelStore toStore, IModelStore fromStore, 
				String sourceUri, String type, boolean excludeLicenseDetails,
				@Nullable String fromNamespace, @Nullable String toNamespace, 
				@Nullable String fromCollectionNamespace, @Nullable String toCollectionNamespace) throws InvalidSPDXAnalysisException;
		
		/**
		 * Copy an item from one Model Object Store to another using the source ID for the target unless it is anonymous
		 * @param toStore Model Store to copy to
		 * @param fromStore Model Store containing the source item
		 * @param sourceObjectUri source object URI
		 * @param type Type to copy
		 * @param externalMap map of URI's to ExternalMaps for any external elements
		 * @param fromNamespace optional namespace of the from property
		 * @param toNamespace optional namespace of the to property
		 * @param fromCollectionNamespace Optional namespace for the from collection to use when creating items for collections.  Defaults to toNamespace
		 * @param toCollectionNamespace Optional namespace for the to collection to use when creating items for collections.  Defaults to toNamespace
		 * @return Object URI for the copied object
		 * @throws InvalidSPDXAnalysisException
		 */
		public TypedValue copy(IModelStore toStore, IModelStore fromStore, 
				String sourceObjectUri, String type,
				@Nullable Map<String, IExternalElementInfo> externalMap,
				@Nullable String fromNamespace, @Nullable String toNamespace,
				@Nullable String fromCollectionNamespace, @Nullable String toCollectionNamespace) throws InvalidSPDXAnalysisException;
		
		/**
			 * Copy an item from one Model Object Store to another
			 * @param toStore Model Store to copy to
			 * @param toId Id to use in the copy
			 * @param toDocumentUri Target document URI
			 * @param fromStore Model Store containing the source item
			 * @param fromDocumentUri Document URI for the source item
			 * @param fromId ID source ID
			 * @param type Type to copy
			 * @param excludeLicenseDetails If true, don't copy over properties of the listed licenses
			 * @param externalMap map of URI's to ExternalMaps for any external elements
			 * @param fromNamespace optional namespace of the from property
			 * @param toNamespace optional namespace of the to property
			 * @param fromCollectionNamespace Optional namespace for the from collection to use when creating items for collections.  Defaults to toNamespace
			 * @param toCollectionNamespace Optional namespace for the to collection to use when creating items for collections.  Defaults to toNamespace
			 * @throws InvalidSPDXAnalysisException
			 */
			public void copy(IModelStore toStore, String toObjectUri, 
					IModelStore fromStore, String fromObjectUri, 
					String type, boolean excludeLicenseDetails,
					@Nullable Map<String, IExternalElementInfo> externalMap,
					@Nullable String fromNamespace, @Nullable String toNamespace,
					@Nullable String fromCollectionNamespace,
					@Nullable String toCollectionNamespace) throws InvalidSPDXAnalysisException;

			/**
			 * Copy an item from one Model Object Store to another
			 * @param toStore Model Store to copy to
			 * @param toObjectUri URI for the destination object
			 * @param fromStore Model Store containing the source item
			 * @param fromObjectUri Object URI for the source item
			 * @param type Type to copy
			 * @param externalMap map of URI's to ExternalMaps for any external elements
			 * @param fromNamespace optional namespace of the from property
			 * @param toNamespace optional namespace of the to property
			 * @param fromCollectionNamespace Optional namespace for the from collection to use when creating items for collections.  Defaults to toNamespace
			 * @param toCollectionNamespace Optional namespace for the to collection to use when creating items for collections.  Defaults to toNamespace
			 * @throws InvalidSPDXAnalysisException
			 */
			public void copy(IModelStore toStore, String toObjectUri, IModelStore fromStore, String fromObjectUri, String type,
					@Nullable Map<String, IExternalElementInfo> externalMap,
					@Nullable String fromNamespace, @Nullable String toNamespace, 
					@Nullable String fromCollectionNamespace, @Nullable String toCollectionNamespace) throws InvalidSPDXAnalysisException;
			
			/**
			 * @param fromStore Store copied from
			 * @param fromObjectUri Object URI in the from tsotre
			 * @param toStore store copied to
			 * @param toNamespace Optional nameSpace used for the copied URI - can copy the same object to multiple namespaces in the same toStore
			 * @return the objectId which has already been copied, or null if it has not been copied
			 */
			public String getCopiedObjectUri(IModelStore fromStore, String fromObjectUri,
					IModelStore toStore, @Nullable String toNamespace);
			
			/**
			 * Record a copied ID between model stores
			 * @param fromStore Store copied from
			 * @param fromObjectUri URI for the from Object
			 * @param toObjectUri URI for the to Object
			 * @param toNamespace Optional nameSpace used for the copied URI - can copy the same object to multiple namespaces in the same toStore
			 * @return any copied to ID for the same stores, URI's, nameSpace and fromID
			 */
			public String putCopiedId(IModelStore fromStore, String fromObjectUri, IModelStore toStore,
					String toObjectUri, @Nullable String toNamespace);
}
