package com.meidusa.amoeba.mongodb.packet;

import java.io.UnsupportedEncodingException;

import org.bson.BSONObject;

import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;

/**
 * <h6><a name="MongoWireProtocol-OPUPDATE"></a>OP_UPDATE <a name="MongoWireProtocol-OPUPDATE"></a></h6>
 * 
 * <p>The OP_UPDATE message is used to update a document in a collection.  The format of a OP_UPDATE message is</p>
 * 
 * <div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
 * <pre class="code-java">struct OP_UPDATE {
 *     MsgHeader header;             <span class="code-comment">// standard message header
 * </span>    int32     ZERO;               <span class="code-comment">// 0 - reserved <span class="code-keyword">for</span> <span class="code-keyword">future</span> use
 * 
 * </span>    cstring   fullCollectionName; <span class="code-comment">// <span class="code-quote">"dbname.collectionname"</span>
 * </span>    int32     flags;              <span class="code-comment">// bit vector. see below
 * </span>    document  selector;           <span class="code-comment">// the query to select the document
 * </span>    document  update;             <span class="code-comment">// specification of the update to perform
 * </span>}
 * </pre>
 * 
 * @author Struct
 *
 */
public class UpdateMongodbPacket extends AbstractMongodbPacket {
	
	public int ZERO = 0;
	public String fullCollectionName;
	public int flags;
	public BSONObject selector;
	public BSONObject update;
	public UpdateMongodbPacket(){
		this.opCode = MongodbPacketConstant.OP_UPDATE;
	}
	protected void init(MongodbPacketBuffer buffer) {
		super.init(buffer);
		buffer.readInt();//ZERO 
		fullCollectionName = buffer.readCString();
		flags = buffer.readInt();
		if(buffer.hasRemaining()){
			selector = buffer.readBSONObject();
		}
		if(buffer.hasRemaining()){
			update = buffer.readBSONObject();
		}
	}

	@Override
	protected void write2Buffer(MongodbPacketBuffer buffer)
			throws UnsupportedEncodingException {
		super.write2Buffer(buffer);
		buffer.writeInt(0);
		buffer.writeCString(fullCollectionName);
		buffer.writeInt(flags);
		if(selector != null){
			buffer.writeBSONObject(selector);
		}
		
		if(update != null){
			buffer.writeBSONObject(update);
		}
	}
	
}
