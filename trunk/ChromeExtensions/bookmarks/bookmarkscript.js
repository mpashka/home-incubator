console.log("bookmarkscript.js");

chrome.extension.onMessage.addListener(function(request, sender, sendResponse) {
    console.log("bookmarkscript.js::onMessage() [" + request.action + "]:" + request);
    if (request.action == "StartBookmarks") {
        createBookmarksHtml(request.bookmarks);
    }
});


var bookmarksModel = [];
var rootBookmarksModel = [];
var bookmarksRootHtmlContainer;
var bookmarksRootHtml;
var bookmarksOpenStatus = [];
var searchInput;

const bookmarkTypeNode = "node";
const bookmarkTypeSubtree = "subtree";

var resizeOn = false;

var bookmarksContextMenu;

var menuWidth = 100;
var menuHeight = 40;
var menuItemBookmarkModel;



/*
Add shortcut Ctrl+B
 */
document.addEventListener('keydown', function(e) {
//    console.log("Keydown: " + e.which);

    if (e.which == 66 && e.ctrlKey) {
//        e.preventDefault();
        console.log("Show bookmarks...");
        if (bookmarksRootHtmlContainer) {
//            console.log("    Bookmarks exists");
            var bookmarksVisible = (!bookmarksRootHtmlContainer.style.display || bookmarksRootHtmlContainer.style.display == 'block');
            bookmarksRootHtmlContainer.style.display = bookmarksVisible ? 'none' : 'block';
        } else {
//            console.log("    Create Bookmarks");
            createBookmarksHtmlPlaceholder();
            sendMessageAndReloadBookmarks("ReloadBookmarks");
        }
    } else if (e.which == 27) {
        checkMenuSize(false);
    }
});



function createBookmarksHtmlPlaceholder() {
    console.log("bookmarkscript.js::createBookmarksHtmlPlaceholder()");

//    dumpCss();

    bookmarksRootHtmlContainer = document.createElement("div");
    bookmarksRootHtmlContainer.id =  "bookmark-div-content";

    var bookmarksContent = document.createElement("div")
    bookmarksContent.id = "bookmark-bookmarks";
    bookmarksRootHtml = document.createElement("ul");
    bookmarksContent.appendChild(bookmarksRootHtml);


    var splitter = document.createElement("div");
    splitter.id = "bookmarsVerticalScrollbar";
    bookmarksRootHtmlContainer.appendChild(splitter);
    splitter.addEventListener('mousedown', function(e) {e.preventDefault();resizeOn = true;});
    document.addEventListener('mouseup', function() {resizeOn = false;});

/*
    document.addEventListener('mouseout', function() {
        console.log("mouse out");
        resizeOn = false;
    });
*/


    bookmarksContextMenu = document.createElement("ul");
    bookmarksContextMenu.id = "bookmarks-contextmenu-div";
    var menuItemCreateSubfolder = document.createElement("li");
    menuItemCreateSubfolder.appendChild(document.createTextNode("Create subfolder"));
    menuItemCreateSubfolder.addEventListener('mousedown', createBookmarkSubfolder);
    bookmarksContextMenu.appendChild(menuItemCreateSubfolder);

    var menuItemRemove = document.createElement("li");
    menuItemRemove.appendChild(document.createTextNode("Remove"));
    menuItemRemove.addEventListener('mousedown', removeBookmark);
    bookmarksContextMenu.appendChild(menuItemRemove);

    bookmarksRootHtmlContainer.appendChild(bookmarksContextMenu);
    document.addEventListener('mousedown', function(e) {checkMenuSize(false);});


    bookmarksRootHtml.addEventListener('contextmenu', showContextMenu);




    var buttonsBar = document.createElement("div");
    buttonsBar.id =  "bookmarsButtonsBar";
    bookmarksRootHtmlContainer.appendChild(buttonsBar);

    var reloadButton = document.createElement("img");
    reloadButton.src = chrome.extension.getURL("images/sync.png");
    reloadButton.addEventListener('click', reloadBookmarksOnClick);
    buttonsBar.appendChild(reloadButton);

    var minimizeButton = document.createElement("img");
    minimizeButton.src = chrome.extension.getURL("images/minimize.png");
    minimizeButton.addEventListener('click', minimizeBookmarksOnClick);
    buttonsBar.appendChild(minimizeButton);

    var p1 = document.createElement("p");
    p1.appendChild(document.createTextNode("Bookmarks"));

    bookmarksRootHtmlContainer.appendChild(p1);


    var searchDiv = document.createElement("div");
    searchDiv.style.position = "relative";
    searchDiv.id = "searchDiv";
    bookmarksRootHtmlContainer.appendChild(searchDiv);
    searchInput = document.createElement("input");
    searchDiv.appendChild(searchInput);
    searchInput.addEventListener('input', onSearch);
    searchInput.addEventListener('keydown', function(e) {
        if (e.which == 27) {    // Handle Esc
            clearSearch();
        }
    });
    var clearSearchButton = document.createElement("img");
    clearSearchButton.src = chrome.extension.getURL("images/cross.png");
//    clearSearchButton.addEventListener('click', reloadBookmarksOnClick);
    clearSearchButton.style.position = "absolute";
    clearSearchButton.style.right = "3px";
    clearSearchButton.style.top = "0";
    clearSearchButton.style.bottom = "0";
    clearSearchButton.style.margin = "auto";
    clearSearchButton.addEventListener('click', clearSearch);

    searchDiv.appendChild(clearSearchButton);
    bookmarksRootHtmlContainer.appendChild(searchDiv);



    bookmarksRootHtmlContainer.appendChild(bookmarksContent);


    document.body.appendChild(bookmarksRootHtmlContainer);
    document.body.addEventListener('mousemove', onMouseMove);
}



function createBookmarksHtml(bookmarks) {
    if (bookmarksRootHtmlContainer) {
        bookmarksRootHtmlContainer.style.display = 'block';
    } else {
        createBookmarksHtmlPlaceholder();
    }
    bookmarksRootHtml.innerHTML = "";
    bookmarksModel = [];
    rootBookmarksModel = [];

    var rootId = 0;
    for (var i = 0; i < bookmarks.length; i++) {
        var bookmarkRoot = bookmarks[i];
        var bookmarkChildren = bookmarkRoot.children;
        if (bookmarkChildren) {
            for (var j = 0; j < bookmarkChildren.length; j++) {
                var bookmark = bookmarkChildren[j];
                var bookmarkModel = createBookmark(0, bookmark, null);
                bookmarksRootHtml.appendChild(bookmarkModel.uiElement);
                rootBookmarksModel[rootId++] = bookmarkModel;
            }
        }
    }
}



function createBookmark(level, bookmark, parentBookmarkModel) {
    var bookmarkModel = bookmark.url ?
            createBookmarkNode(bookmark, parentBookmarkModel.siteIconsLoaded)
            :createBookmarkSubtree(level, bookmark, parentBookmarkModel);

    bookmarksModel[bookmark.id] = bookmarkModel;

    bookmarkModel.bookmark = bookmark;
    bookmarkModel.parentBookmarkModel = parentBookmarkModel;
//    bookmarkModel.filtered = true;

    return bookmarkModel;
}



function createBookmarkNode(bookmark, visible) {
//    console.log(bookmark.url + " '" + bookmark.title + "':" + (bookmark.title ? bookmark.title.length : "empty"));

    var li = document.createElement("li");
    var a = document.createElement("a");
    a.setAttribute("href", bookmark.url);
    var title = bookmark.title;
    if (!title || title.length == 0) {
        title = bookmark.url;
    }

    a.appendChild(document.createTextNode(title));

    if (visible) {
        var urlHost = getSiteUrl(bookmark.url);
        var siteIcon = document.createElement("img");
        siteIcon.src = urlHost + "/favicon.ico";
        li.appendChild(siteIcon);
    }

    li.appendChild(a);


    li.addEventListener('dragstart', onBookmarkDragStart);
    li.addEventListener('dragover', onBookmarkDragOver);
    li.addEventListener('dragleave', onBookmarkDragLeave);
    li.addEventListener('drop', onBookmarkDrop);

    li.draggable = true;

    var bookmarkModel = {uiElement: li, type: bookmarkTypeNode};
    li.bookmarkModel = bookmarkModel;
    li.className = "bookmarkItem";


    return bookmarkModel;
}



function createBookmarkSubtree(level, bookmark, parentBookmarkModel) {
    var li = document.createElement("li");

    var bookmarkId = bookmark.id;
    var treeOpened, parentVisible;
    if (level <= 0) {
        treeOpened = parentVisible = true;
    } else {
        treeOpened = bookmarksOpenStatus[bookmarkId];
        parentVisible = parentBookmarkModel.siteIconsLoaded;
    }

    var toggle = document.createElement("a");
    toggle.className = treeOpened ? "bookmarkToggle opened" : "bookmarkToggle closed";
//    toggle.addEventListener('click', toggleBookmarkSubtree);
    toggle.style.backgroundImage = "url(" + chrome.extension.getURL("images/toggle_sprite.png") + ")";

    var htmlSubtreeTitle = document.createElement("div");
    htmlSubtreeTitle.appendChild(toggle);
    htmlSubtreeTitle.appendChild(document.createTextNode(bookmark.title));
    htmlSubtreeTitle.className = "bookmarkFolderTitle";
    htmlSubtreeTitle.addEventListener('click', toggleBookmarkSubtree);
    htmlSubtreeTitle.draggable = true;
    htmlSubtreeTitle.addEventListener('dragstart', onBookmarkDragStart);
    htmlSubtreeTitle.addEventListener('dragover', onBookmarkDragOver);
    htmlSubtreeTitle.addEventListener('dragleave', onBookmarkDragLeave);
    htmlSubtreeTitle.addEventListener('drop', onBookmarkDrop);


    var ul = document.createElement("ul");
    ul.style.display = treeOpened ? 'block' : 'none';
    var visible = treeOpened && parentVisible;

    var bookmarkModel = {uiElement: li, type: bookmarkTypeSubtree, subtreeUiElement: ul, subtreeUiToggle: toggle
        , opened: treeOpened, siteIconsLoaded: visible};
//    toggle.bookmarkModel = bookmarkModel;     // for open/close
    htmlSubtreeTitle.bookmarkModel = bookmarkModel;     // for open/close


    var bookmarkChildren = bookmark.children;
    if (bookmarkChildren) {
        var bookmarkChildModels = [];
        for (var i = 0; i < bookmarkChildren.length; i++) {
            var bookmarkChildNode = bookmarkChildren[i];
            var bookmarkChildModel = createBookmark(level+1, bookmarkChildNode, bookmarkModel);
            ul.appendChild(bookmarkChildModel.uiElement);
            bookmarkChildModels[i] = bookmarkChildModel;
        }
        bookmarkModel.childBookmarks = bookmarkChildModels;
    }

    li.appendChild(htmlSubtreeTitle);
    li.appendChild(ul);
    return bookmarkModel;
}



//
//
//

/**
 * Toggles the display of nodes given the status of their associated controls.
 */
function toggleBookmarkSubtree(e) {
    var target = e.currentTarget;
    var bookmarkModel = target.bookmarkModel;
    if (!bookmarkModel) {
        console.log("Error. No model in toggle: " + target);
//        dumpObject("Event Target Object", target);
        dumpObject("Event", e);
        return;
    }
    var uiElement = bookmarkModel.subtreeUiElement;
    var toggle = bookmarkModel.subtreeUiToggle; 
    var opened = !bookmarkModel.opened;
    var bookmark = bookmarkModel.bookmark;
    var bookmarkId = bookmark.id;
    bookmarksOpenStatus[bookmarkId] = opened;


    toggle.className = opened ? "bookmarkToggle opened" : "bookmarkToggle closed";
    uiElement.style.display = opened ? "block" : "none";
    bookmarkModel.opened = opened;

    if (!bookmarkModel.siteIconsLoaded) {
        loadSiteIcons(bookmarkModel);
    }
}


function loadSiteIcons(parentBookmarkModel) {
    parentBookmarkModel.siteIconsLoaded = true;

    var childBookmarkModels = parentBookmarkModel.childBookmarks;
    if (!childBookmarkModels) {
        return;
    }
    for (var i = 0; i < childBookmarkModels.length; i++) {
        var bookmarkModel = childBookmarkModels[i];
        var bookmark = bookmarkModel.bookmark;

        if (!bookmark.url) {
            continue;
        }
        var urlHost = getSiteUrl(bookmark.url);
        if (!urlHost) {
            continue;
        }

        var siteIcon = document.createElement("img");
        siteIcon.src = urlHost + "/favicon.ico";

        var li = bookmarkModel.uiElement;
        var a = li.childNodes[0];
        a.insertBefore(siteIcon, a.childNodes[0]);
    }
}

const urlRegexp = /(^[^:]+:\/+[^\/]+)(\/.*)?/;
function getSiteUrl(fullSiteUrl) {
    var urlParts = urlRegexp.exec(fullSiteUrl);
    var urlHost;
    if (urlParts && urlParts[1]) {
        urlHost = urlParts[1];
    } else {
        console.log("Can't parse URL: " + fullSiteUrl);
    }
    return urlHost;
}

//
// Drag and Drop
//

function onBookmarkDragStart(event) {
//    event.preventDefault();
    var dragTarget = event.currentTarget;
    var bookmarkModel = dragTarget.bookmarkModel;
    if (!bookmarkModel) {
        console.log("No bookmark model in drag start target");
        dumpObject("DragStartEvent", event);
        return;
    }
    var bookmark = bookmarkModel.bookmark;
    var bookmarkId = bookmark.id;

//    event.dataTransfer.effectAllowed='move';
//    event.dataTransfer.dropEffect='move';
    event.dataTransfer.setData('bookmarkId', bookmarkId);
//    event.dataTransfer.setDragImage(ev.target,0,0);

    console.log("Drag start [" + bookmark.id + "]: " + bookmark.title);
}

function onBookmarkDrop(event) {
    event.preventDefault();
    console.log("Drop");
    var dropTarget = event.currentTarget;
    dropTarget.classList.remove("bookmarkDrag");
    var targetBookmarkModel = dropTarget.bookmarkModel;
    if (!targetBookmarkModel) {
        console.warn("No bookmark model in target");
        return;
    }
    var targetBookmark = targetBookmarkModel.bookmark;
    var targetType = targetBookmarkModel.type;
    var targetBookmarkLocation;
    if (targetType == bookmarkTypeNode) {
        //        move src before target
        targetBookmarkLocation = {parentId: targetBookmark.parentId, index: targetBookmark.index};
    } else if (targetType == bookmarkTypeSubtree) {
        // move src as last in target
        targetBookmarkLocation = {parentId: targetBookmark.id};
    } else {
        console.error("Unknown target:" + targetType);
        return;
    }

    var bookmarkId = event.dataTransfer.getData('bookmarkId');
    if (bookmarkId) {
        var srcBookmarkModel = bookmarksModel[bookmarkId];
        var srcBookmark = srcBookmarkModel.bookmark;

        console.log("    Target: " + srcBookmark.title + " -> " + targetBookmark.title);
        console.log("        targetType:" + targetType + " node:" + (targetType == bookmarkTypeNode)
                + ", subtree: " + (targetType == bookmarkTypeSubtree));

        var moveBookmarkAction = {srcBookmarkId: srcBookmark.id, targetBookmarkLocation: targetBookmarkLocation};
        sendMessageAndReloadBookmarks("MoveBookmark", moveBookmarkAction);
    } else {
        // Just save link
        var linkUrl = event.dataTransfer.getData('text/uri-list');
        var title;
        if (document.URL == linkUrl) {
            title = document.title;
/*
        var siteIcon;
            var icons = document.querySelectorAll( 'link[rel="icon"], link[rel="shortcut icon"]' );
            for (var i = 0; i < icons.length; i++) {
                icons[i].getAttribute("src");
            }
*/
        }

        var addBookmark = {
            title: title,
            parentId: targetBookmarkLocation.parentId,
            index: targetBookmarkLocation.index,
            url: linkUrl
        };

        sendMessageAndReloadBookmarks("AddBookmark", {bookmark: addBookmark});
    }
}

function onBookmarkDragOver(event) {
    event.preventDefault();
    var element = event.currentTarget;
    element.classList.add("bookmarkDrag");
//    console.log("DragOver");
}

function onBookmarkDragLeave(event) {
    event.preventDefault();
    var element = event.currentTarget;
    element.classList.remove("bookmarkDrag");
//    console.log("DragLeave");
}


//
//
//

function reloadBookmarksOnClick(event) {
    sendMessageAndReloadBookmarks("ReloadBookmarks");
}


function sendMessageAndReloadBookmarks(action, request) {
    request = request || {};
    request.action = action;
    var div = bookmarksRootHtml.parentNode;
    var originalScrollTop = div.scrollTop;
    chrome.extension.sendRequest(null, request, function(response) {
        console.log("ReloadBookmarks response received");
        if (!response) {
            console.warn("    Error occur: " + chrome.extension.lastError);
            if (chrome.extension.lastError && chrome.extension.lastError.message) {
                console.log("        : " + chrome.extension.lastError.message);
            }
            return;
        }
        var bookmarks = response.bookmarks;
        createBookmarksHtml(bookmarks);
        div.scrollTop = originalScrollTop;
    });
}


function minimizeBookmarksOnClick() {
    console.log("Minimize");
    if (bookmarksRootHtmlContainer) {
        bookmarksRootHtmlContainer.style.display = 'none';
    }
}


//
//
//

function onMouseMove(event) {
    if (!resizeOn) {
        return;
    }
    event.preventDefault();
    var newWidth = event.clientX + document.body.scrollLeft - 20;
    if (newWidth < 100 || newWidth > document.body.clientWidth / 2) {
        return;
    }
    bookmarksRootHtmlContainer.style.width = newWidth + "px";
}



function showContextMenu(e) {
    e.preventDefault();
    checkMenuSize(true);

    var menuTarget = e.target;
    menuItemBookmarkModel = menuTarget.bookmarkModel;
    if (!menuItemBookmarkModel) {
        console.log("No bookmark in target: " + menuItemBookmarkModel);
        return;
    }


    var x = e.clientX, y = e.clientY;
    if (bookmarksRootHtmlContainer.offsetWidth - x < menuWidth) {
        x = x > menuWidth ? x - menuWidth : 0;
    }

    if (bookmarksRootHtmlContainer.offsetHeight - y < menuHeight) {
        y = y > menuHeight ? y - menuHeight : 0;
    }

    bookmarksContextMenu.style.left = x + 'px';
    bookmarksContextMenu.style.top = y + 'px';
    bookmarksContextMenu.style.display = 'block';
}



function createBookmarkSubfolder(e) {
    bookmarksContextMenu.style.display = 'none';
    var bookmarkModel = menuItemBookmarkModel;
    menuItemBookmarkModel = null;

    e.preventDefault();

    if (!bookmarkModel) {
        console.warn("No bookmark");
        return;
    }
    var subtreeName = window.prompt("Bookmark folder name?", "");
    if (!subtreeName || subtreeName.length == 0) {
        return;
    }

    var bookmark = bookmarkModel.bookmark;

    var addBookmark = {title: subtreeName};
    if (bookmarkModel.type == bookmarkTypeSubtree) {
        addBookmark.parentId = bookmark.id;
    } else if (bookmarkModel.type == bookmarkTypeNode) {
        addBookmark.parentId = bookmark.parentId;
        addBookmark.index = bookmark.index;
    }


    sendMessageAndReloadBookmarks("AddBookmark", {bookmark: addBookmark});
}


function removeBookmark(e) {
    bookmarksContextMenu.style.display = 'none';
    var bookmarkModel = menuItemBookmarkModel;
    menuItemBookmarkModel = null;

    e.preventDefault();

    if (!bookmarkModel) {
        console.warn("No bookmark");
        return;
    }

    var bookmark = bookmarkModel.bookmark;
    var removeOk = window.confirm("Are you sure to remove "
            + (bookmarkModel.type == bookmarkTypeSubtree ? "bookmark folder: " : "bookmark: ")
            + bookmark.title);
    if (!removeOk) {
        return;
    }


    var action = bookmarkModel.type == bookmarkTypeSubtree ? "RemoveBookmarkSubtree" : "RemoveBookmark";
    sendMessageAndReloadBookmarks(action, {bookmarkId: bookmark.id});
}


function checkMenuSize(visible) {
    if (bookmarksContextMenu.style.display == 'block') {
        menuWidth = bookmarksContextMenu.offsetWidth;
        menuHeight = bookmarksContextMenu.offsetHeight;
        if (!visible) {
            bookmarksContextMenu.style.display = 'none';
            menuItemBookmarkModel = null;
        }
    }
}


//
//
//

var prevSearchValue;

function clearSearch() {
    searchInput.value = null;
    onSearch(null);
}


function onSearch(e) {
    var searchValue = searchInput.value;
    if (prevSearchValue == searchValue) {
        return;
    }
    prevSearchValue = searchValue;
    var clearFilter = !searchValue || searchValue.length == 0;
    var searchWords;
    if (!clearFilter) {
        searchWords = searchValue.split(' ');
        for (var i = 0; i < searchWords.length; i++) {
            searchWords[i] = searchWords[i].toLowerCase();
        }
    }
    cycleRootNodes(function(bookmarkModel, nodeResults) {
        if (clearFilter) {
            setBookmarkVisible(bookmarkModel, true);
            return true;
        }
        var visible = false;
        for (var i = 0; i < nodeResults.length; i++) {
            if (nodeResults[i]) {
                visible = true;
                break;
            }
        }
        if (!visible) {
            visible = searchText(bookmarkModel, searchWords);
        }
        setBookmarkVisible(bookmarkModel, visible);
        return visible;
    }, function(bookmarkModel) {
        if (clearFilter) {
            setBookmarkVisible(bookmarkModel, true);
            return true;
        }
        var visible = searchText(bookmarkModel, searchWords);
        setBookmarkVisible(bookmarkModel, visible);
        return visible;
    });
}

function searchText(bookmarkModel, searchWords) {
    for (var i = 0; i < searchWords.length; i++) {
        var word = searchWords[i];
        if (word.length == 0) {
            continue;
        }
        var bookmark = bookmarkModel.bookmark;
        if (!(bookmark.title && bookmark.title.toLowerCase().indexOf(word) >= 0) &&
                !(bookmark.url && bookmark.url.toLowerCase().indexOf(word) >= 0))
        {
            return false;
        }
    }
    return true;
}

function cycleRootNodes(subtreeCallback, nodeCallback) {
    for (var i = 0; i < rootBookmarksModel.length; i++) {
        var bookmarksModel = rootBookmarksModel[i];
        cycleNodes(bookmarksModel, subtreeCallback, nodeCallback);
    }
}

function cycleNodes(bookmarkModel, subtreeCallback, nodeCallback) {
    if (bookmarkModel.type == bookmarkTypeSubtree) {
        var childBookmarksModel = bookmarkModel.childBookmarks;
        var nodeResults = []
        for (var i = 0; i < childBookmarksModel.length; i++) {
            var childBookmarkModel = childBookmarksModel[i];
            nodeResults[i] = cycleNodes(childBookmarkModel, subtreeCallback, nodeCallback);
        }
        return subtreeCallback(bookmarkModel, nodeResults);
    } else if (bookmarkModel.type == bookmarkTypeNode) {
        return nodeCallback(bookmarkModel);
    }
}


/**
 * Used to hide/show filtered element
 *
 * @param bookmarkModel
 */
function setBookmarkVisible(bookmarkModel, visible) {
    var uiElement = bookmarkModel.uiElement;
    uiElement.style.display = visible ? "block" : "none";
}




//
//
//

function dumpArray(name, event) {
    console.log("DumpArray: " + name);
    for (var i = 0; i < event.length; i++) {
        dumpObject("     [" + i + "]", event[i]);
    }
}

function dumpObject(name, event) {
    console.log("DumpObject: " + name);
    for (var i in event) {
        console.log("    " + i + ": " + event[i]);
    }
    console.log("-----------------------------------------");
}

function dumpCss() {
    console.log("Dump CSS: " + document.styleSheets + ", " + document.styleSheets.length);
    var sss = document.styleSheets;
    for (var i = 0; i < sss.length; i++) {
        console.log("styleSheets[" + i + "]");
        var ss = sss[i];
        for(var key in ss) {
            var value = ss[key];
            console.log("    " + key + "=" + value);
        }
        var rules = ss.cssRules;
        console.log("-------------------------------- CSS Rules");
        if (rules) {
            for (var j = 0; j < rules.length; j++) {
                console.log("    rule[" + i + "]");

                var rule = rules[j];
                for(var key1 in rule) {
                    var value1 = rule[key1];
                    console.log("        " + key1 + "=" + value1);
                }
            }
        } else {
            console.log("    no rules");
        }
        console.log("--------------------------------");

    }

}

