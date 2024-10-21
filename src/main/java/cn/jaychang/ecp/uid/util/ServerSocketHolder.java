package cn.jaychang.ecp.uid.util;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.net.ServerSocket;

/**
 * ServerSocketHolder
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
public class ServerSocketHolder implements Serializable {
    private static final long serialVersionUID = -5960929890708594584L;

    private ServerSocket serverSocket;

}
