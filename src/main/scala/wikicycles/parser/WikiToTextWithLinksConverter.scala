package wikicycles.parser

import info.bliki.htmlcleaner.{ContentToken, TagNode}
import info.bliki.wiki.filter.{ITextConverter, WPList, WPTable}
import info.bliki.wiki.model.{Configuration, IWikiModel, ImageFormat}
import info.bliki.wiki.tags.{RefTag, WPATag, WPTag}

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
            item match {
              case list: java.util.List[_] =>
                nodesToText(list, resultBuffer, model)
              case contentToken: ContentToken =>
                resultBuffer.append(contentToken.getContent)
              case _: RefTag => // Ignore ref tags
              case list: WPList =>
                list.renderPlainText(this, resultBuffer, model)
              case _: WPTable => // Ignore tables
              case tagNode: TagNode =>
                appendBodyString(tagNode, resultBuffer)
              case _ => // Ignore everything else
            }
          }
        }
      }
      finally {
        model.decrementRecursionLevel
      }
    }
  }

  private def appendBodyString(node: TagNode, buf: Appendable): Unit = {
    node match {
      case n: WPATag =>
        val link = node.asInstanceOf[WPATag].getAttributes.get("title")
        if (link != null) {
          if (!link.contains(":")) {
            buf.append("[[" + link + "]]")
          }
        }
      case n: RefTag => // Ignore ref tags
      case _ =>
        val children = node.getChildren
        var i = 0
        while (i < children.size) {
          children.get(i) match {
            case child: ContentToken =>
              buf.append(child.getContent)
            case child: TagNode => appendBodyString(child, buf)
            case _ => // Do nothing
          }
          i += 1
        }
    }
  }

  override def imageNodeToText(imageTagNode: TagNode, imageFormat: ImageFormat, resultBuffer: Appendable, model: IWikiModel): Unit = {
    // Nothing to do
  }
}
