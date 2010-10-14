/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.net;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.data.AuthCodes;
import com.meidusa.amoeba.server.AuthenticateFilter;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public abstract class Authenticator {

    protected static Logger                log = Logger.getLogger(Authenticator.class);
    private AuthenticateFilter             filter;
	protected String user;
	protected String password;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
    public boolean authenticateConnection(final AuthingableConnection conn,byte[] authenMessage) {
        final AuthResponseData rdata = createResponseData();
        try {
            if (doFilte(conn, rdata)) {
                processAuthentication(conn, authenMessage,rdata);
                return true;
            }else{
            	return false;
            }
        } catch (Exception e) {
            log.warn("Error authenticating", e);
            rdata.code = AuthCodes.SERVER_ERROR;
            rdata.message = e.getMessage();
            return false;
        } finally {
        	conn.afterAuthing(rdata);
        }
    }

    public AuthenticateFilter getFilter() {
        return filter;
    }

    public void setFilter(AuthenticateFilter filter) {
        this.filter = filter;
    }

    protected AuthResponseData createResponseData() {
        return new AuthResponseData();
    }

    protected boolean doFilte(AuthingableConnection conn, AuthResponseData rdata) {
        return (filter != null) ? filter.doFilte(conn, rdata) : true;
    }

    /**
     * 处理连接身份验证
     * 
     * @param conn 需要身份验证的连接
     * @param rdata 需要反馈的数据
     */
    protected abstract void processAuthentication(AuthingableConnection conn, byte[] authenMessage,AuthResponseData rdata);
}
