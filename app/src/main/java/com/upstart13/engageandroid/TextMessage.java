//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.upstart13.engageandroid;

import java.util.Date;

public class TextMessage
{
    public enum Direction {sent, received}

    public String _groupId;
    public Date _ts;
    public Direction _direction;
    public String _sourceNodeId;
    public String _messageText;
}
