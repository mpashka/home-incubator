// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.


// Useful
//var resp = JSON.parse(response.farewell);
// wrong: document.getElementById("resp").innerHTML = response.farewell;
//document.getElementById("resp").innerText = response.farewell;

console.log("background.js");
if (chrome.browserAction && chrome.extension) {
    console.log("    Document loaded. Init...");
    initBookmarksExtension();
} else {
    console.log("    Chrome extensions not loaded. Please reload extension...");
    alert("Chrome extensions not loaded. Please reload extension...");
}


function initBookmarksExtension() {
    console.log("background.js::initBookmarksExtension()");
    chrome.browserAction.onClicked.addListener(showBookmarks);

    console.log("    Add listener...");
    chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
        var tab = sender.tab;
        var id = sender.id;
        console.log("background.js. Tab:" + tab.id + " ID: " + id);
        console.log("background.js::onMessage(): " + request);

        var action = request.action;
        if (!action) {
            console.error("No action specified in request");
        } else if (action == "ReloadBookmarks") {
            console.log("background.js::ReloadBookmarks: " + tab);
            chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
                var bookmarksPort = tab;
                sendResponse({"bookmarks" : bookmarkTreeNodes});
            });
        } else if (action == "MoveBookmark") {
            var srcBookmarkId = request.srcBookmarkId;
            var targetBookmarkLocation = request.targetBookmarkLocation;
            chrome.bookmarks.move(srcBookmarkId, targetBookmarkLocation, function() {
                chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
                    var bookmarksPort = tab;
                    sendResponse({"bookmarks" : bookmarkTreeNodes});
                });
            })
        } else if (action == "AddBookmark") {
            var bookmark = request.bookmark;
            if (!bookmark) {
                console.error("No bookmark in request");
                return;
            }
            chrome.bookmarks.create(bookmark, function() {
                chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
                    var bookmarksPort = tab;
                    sendResponse({"bookmarks" : bookmarkTreeNodes});
                });
            })
        } else if (action == "RemoveBookmark") {
            var bookmarkId = request.bookmarkId;
            if (!bookmarkId) {
                console.error("No bookmark in request");
                return;
            }
            console.log("Remove bookmark: " + bookmarkId);
            chrome.bookmarks.remove(bookmarkId, function() {
                chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
                    var bookmarksPort = tab;
                    sendResponse({"bookmarks" : bookmarkTreeNodes});
                });
            })
        } else if (action == "RemoveBookmarkSubtree") {
            bookmarkId = request.bookmarkId;
            if (!bookmarkId) {
                console.error("No bookmark in request");
                return;
            }
            console.log("Remove bookmark tree: " + bookmarkId);
            chrome.bookmarks.removeTree(bookmarkId, function() {
                chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
                    var bookmarksPort = tab;
                    sendResponse({"bookmarks" : bookmarkTreeNodes});
                });
            })
        }
    });
}

function showBookmarks(tab) {
    console.log("background.js::showBookmarks: " + tab);

    chrome.bookmarks.getTree(function(bookmarkTreeNodes) {
//      dumpBookmarks("", bookmarkTreeNodes);
        var bookmarksPort = tab;
        sendMessageToContent(bookmarksPort, tab, "StartBookmarks", {"bookmarks" : bookmarkTreeNodes});
    });
}

function dumpBookmarks(indent, bookmarkTreeNodes) {
    for (var i = 0; i < bookmarkTreeNodes.length; i++) {
        var bookmarkTreeNode = bookmarkTreeNodes[i];

        console.log(indent + "    [" + bookmarkTreeNode.id + "] " + bookmarkTreeNode.title + " = " + bookmarkTreeNode.url);
        if (bookmarkTreeNode.children) {
            dumpBookmarks("    " + indent, bookmarkTreeNode.children);
        }
    }
}

function sendMessageToContent(port, tab, action, msg) {
    msg = msg || {};
    msg.action = action;
    console.log("    posting show bookmarks action[" + action + "] :" + msg);
    chrome.tabs.sendMessage(tab.id, msg, function(response) {
        console.log("    background.js::onResponse(): " + response);
    });
}
