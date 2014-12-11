package wikicycles

import java.util.List

import info.bliki.htmlcleaner.{TagNode, Utils, ContentToken}
import info.bliki.wiki.filter.{WPTable, WPList, ITextConverter}
import info.bliki.wiki.model.{ImageFormat, Configuration, IWikiModel}
import info.bliki.wiki.tags.{RefTag, WPATag}

/**
 * Created by mg on 08.12.14.
 */
class WikiToTextWithLinksConverter extends ITextConverter {
  override def noLinks(): Boolean = false

  override def nodesToText(nodes: java.util.List[_], resultBuffer: Appendable, model: IWikiModel): Unit = {
    if (nodes != null && !nodes.isEmpty) {
      try {
        val level: Int = model.incrementRecursionLevel
        if (level > Configuration.RENDERER_RECURSION_LIMIT) {
          return
        }
        val childrenIt = nodes.iterator
        while (childrenIt.hasNext) {
          val item = childrenIt.next
          if (item != null) {
            if (item.isInstanceOf[java.util.List[_]]) {
              val list = item.asInstanceOf[java.util.List[_]]
              nodesToText(list, resultBuffer, model)
            } else if (item.isInstanceOf[ContentToken]) {
              val contentToken: ContentToken = item.asInstanceOf[ContentToken]
              resultBuffer.append(contentToken.getContent)
            } else if (item.isInstanceOf[RefTag]) {
              // Ignore ref tags
            } else if (item.isInstanceOf[WPList]) {
              item.asInstanceOf[WPList].renderPlainText(this, resultBuffer, model)
            } else if (item.isInstanceOf[WPTable]) {
              item.asInstanceOf[WPTable].renderPlainText(this, resultBuffer, model)
            } else if (item.isInstanceOf[TagNode]) {
              getBodyString(item.asInstanceOf[TagNode], resultBuffer)
            }
          }
        }
      }
      finally {
        model.decrementRecursionLevel
      }
    }
  }

  private def getBodyString(node: TagNode, buf: Appendable): Unit = {
    if (node.isInstanceOf[WPATag]) {
      val link = node.asInstanceOf[WPATag].getAttributes.get("title")
      if (link != null) {
        if (!link.contains(":")) {
          buf.append("[[" + link + "]]")
        }
      }
    } else if (node.isInstanceOf[RefTag]) {
      // Ignore ref tags
    } else {
      val children = node.getChildren
      var i = 0
      while (i < children.size) {
        val child = children.get(i)
        if (child.isInstanceOf[ContentToken]) {
          buf.append((child.asInstanceOf[ContentToken]).getContent)
        } else if (child.isInstanceOf[TagNode]) {
          getBodyString(child.asInstanceOf[TagNode], buf)
        }
        i += 1
      }
    }
  }

  override def imageNodeToText(imageTagNode: TagNode, imageFormat: ImageFormat, resultBuffer: Appendable, model: IWikiModel): Unit = {
    // Nothing to do
  }
}
