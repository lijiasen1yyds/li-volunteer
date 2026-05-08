$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$buildDir = Join-Path $root 'tmp_docx_build\system_function_design'
$mediaDir = Join-Path $buildDir 'word\media'
$relsDir = Join-Path $buildDir 'word\_rels'
$docPropsDir = Join-Path $buildDir 'docProps'
$pkgRelsDir = Join-Path $buildDir '_rels'

if (Test-Path $buildDir) {
    Remove-Item -LiteralPath $buildDir -Recurse -Force
}

New-Item -ItemType Directory -Path $mediaDir -Force | Out-Null
New-Item -ItemType Directory -Path $relsDir -Force | Out-Null
New-Item -ItemType Directory -Path $docPropsDir -Force | Out-Null
New-Item -ItemType Directory -Path $pkgRelsDir -Force | Out-Null

Add-Type @"
using System;
using System.Drawing;
using System.Drawing.Drawing2D;

public static class GraphicsExtensions {
    public static void DrawRoundedRectangle(this Graphics graphics, Pen pen, Rectangle bounds, int radius) {
        using (GraphicsPath path = BuildPath(bounds, radius)) {
            graphics.DrawPath(pen, path);
        }
    }

    public static void FillRoundedRectangle(this Graphics graphics, Brush brush, Rectangle bounds, int radius) {
        using (GraphicsPath path = BuildPath(bounds, radius)) {
            graphics.FillPath(brush, path);
        }
    }

    private static GraphicsPath BuildPath(Rectangle bounds, int radius) {
        int diameter = radius * 2;
        GraphicsPath path = new GraphicsPath();
        path.AddArc(bounds.X, bounds.Y, diameter, diameter, 180, 90);
        path.AddArc(bounds.Right - diameter, bounds.Y, diameter, diameter, 270, 90);
        path.AddArc(bounds.Right - diameter, bounds.Bottom - diameter, diameter, diameter, 0, 90);
        path.AddArc(bounds.X, bounds.Bottom - diameter, diameter, diameter, 90, 90);
        path.CloseFigure();
        return path;
    }
}
"@

function Escape-XmlText {
    param([string]$Text)
    if ($null -eq $Text) {
        return ''
    }
    return [System.Security.SecurityElement]::Escape($Text)
}

function Add-ParagraphXml {
    param(
        [string]$Text,
        [int]$FontSize = 24,
        [string]$Justification = 'left',
        [bool]$Bold = $false,
        [int]$SpacingBefore = 0,
        [int]$SpacingAfter = 120,
        [int]$FirstLineIndent = 0
    )
    $textXml = Escape-XmlText $Text
    $boldXml = ''
    if ($Bold) {
        $boldXml = '<w:b/>'
    }
    $indXml = ''
    if ($FirstLineIndent -gt 0) {
        $indXml = "<w:ind w:firstLineChars='$FirstLineIndent' w:firstLine='$($FirstLineIndent * $FontSize / 2)'/>"
    }
    return "<w:p><w:pPr><w:jc w:val='$Justification'/><w:spacing w:before='$SpacingBefore' w:after='$SpacingAfter' w:line='360' w:lineRule='auto'/>$indXml</w:pPr><w:r><w:rPr>$boldXml<w:rFonts w:ascii='Times New Roman' w:hAnsi='Times New Roman' w:eastAsia='SimSun'/><w:sz w:val='$FontSize'/><w:szCs w:val='$FontSize'/></w:rPr><w:t xml:space='preserve'>$textXml</w:t></w:r></w:p>"
}

function Add-ImageParagraphXml {
    param(
        [string]$RelationshipId,
        [int]$DocPrId,
        [string]$Name,
        [int]$Cx,
        [int]$Cy
    )
    return @"
<w:p>
  <w:pPr>
    <w:jc w:val='center'/>
    <w:spacing w:before='60' w:after='180'/>
  </w:pPr>
  <w:r>
    <w:drawing>
      <wp:inline distT='0' distB='0' distL='0' distR='0'>
        <wp:extent cx='$Cx' cy='$Cy'/>
        <wp:effectExtent l='0' t='0' r='0' b='0'/>
        <wp:docPr id='$DocPrId' name='$Name'/>
        <wp:cNvGraphicFramePr>
          <a:graphicFrameLocks xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main' noChangeAspect='1'/>
        </wp:cNvGraphicFramePr>
        <a:graphic xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main'>
          <a:graphicData uri='http://schemas.openxmlformats.org/drawingml/2006/picture'>
            <pic:pic xmlns:pic='http://schemas.openxmlformats.org/drawingml/2006/picture'>
              <pic:nvPicPr>
                <pic:cNvPr id='0' name='$Name'/>
                <pic:cNvPicPr/>
              </pic:nvPicPr>
              <pic:blipFill>
                <a:blip r:embed='$RelationshipId'/>
                <a:stretch><a:fillRect/></a:stretch>
              </pic:blipFill>
              <pic:spPr>
                <a:xfrm>
                  <a:off x='0' y='0'/>
                  <a:ext cx='$Cx' cy='$Cy'/>
                </a:xfrm>
                <a:prstGeom prst='rect'><a:avLst/></a:prstGeom>
              </pic:spPr>
            </pic:pic>
          </a:graphicData>
        </a:graphic>
      </wp:inline>
    </w:drawing>
  </w:r>
</w:p>
"@
}

function Draw-CenteredText {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Text,
        [System.Drawing.Font]$Font,
        [System.Drawing.Brush]$Brush,
        [System.Drawing.RectangleF]$Rect
    )
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $Graphics.DrawString($Text, $Font, $Brush, $Rect, $format)
    $format.Dispose()
}

function Draw-ModuleDiagram {
    param(
        [string]$OutputPath,
        [string]$Title,
        [string]$RootLabel,
        [string[]]$Items,
        [System.Drawing.Color]$AccentColor
    )

    $width = 1280
    $height = 880
    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $graphics.Clear([System.Drawing.Color]::FromArgb(250, 251, 253))

    $titleFont = New-Object System.Drawing.Font('Microsoft YaHei', 22, [System.Drawing.FontStyle]::Bold)
    $rootFont = New-Object System.Drawing.Font('Microsoft YaHei', 18, [System.Drawing.FontStyle]::Bold)
    $itemFont = New-Object System.Drawing.Font('Microsoft YaHei', 14)
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(35, 35, 35))
    $whiteBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $linePen = New-Object System.Drawing.Pen ($AccentColor, 4)
    $borderPen = New-Object System.Drawing.Pen ($AccentColor, 2)
    $itemBorderPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(170, $AccentColor.R, $AccentColor.G, $AccentColor.B), 2)
    $itemFill = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(245, 248, 252))
    $accentFill = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(236, $AccentColor.R, $AccentColor.G, $AccentColor.B))

    Draw-CenteredText $graphics $Title $titleFont $brush ([System.Drawing.RectangleF]::new(0, 24, $width, 40))

    $rootRect = [System.Drawing.Rectangle]::new(450, 120, 380, 90)
    $graphics.FillRoundedRectangle($accentFill, $rootRect, 18)
    $graphics.DrawRoundedRectangle($borderPen, $rootRect, 18)
    Draw-CenteredText $graphics $RootLabel $rootFont $brush ([System.Drawing.RectangleF]::new($rootRect.X, $rootRect.Y, $rootRect.Width, $rootRect.Height))

    $itemRects = @(
        [System.Drawing.Rectangle]::new(90, 290, 290, 92),
        [System.Drawing.Rectangle]::new(495, 290, 290, 92),
        [System.Drawing.Rectangle]::new(900, 290, 290, 92),
        [System.Drawing.Rectangle]::new(90, 580, 290, 92),
        [System.Drawing.Rectangle]::new(495, 580, 290, 92),
        [System.Drawing.Rectangle]::new(900, 580, 290, 92)
    )

    for ($i = 0; $i -lt $Items.Count; $i++) {
        $rect = $itemRects[$i]
        $rootCenterX = $rootRect.X + ($rootRect.Width / 2)
        $rootCenterY = $rootRect.Y + $rootRect.Height
        $itemCenterX = $rect.X + ($rect.Width / 2)
        $itemCenterY = $rect.Y
        $graphics.DrawLine($linePen, $rootCenterX, $rootCenterY, $itemCenterX, $itemCenterY)
        $graphics.FillRoundedRectangle($itemFill, $rect, 16)
        $graphics.DrawRoundedRectangle($itemBorderPen, $rect, 16)
        Draw-CenteredText $graphics $Items[$i] $itemFont $brush ([System.Drawing.RectangleF]::new($rect.X + 10, $rect.Y + 10, $rect.Width - 20, $rect.Height - 20))
    }

    $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)

    $linePen.Dispose()
    $borderPen.Dispose()
    $itemBorderPen.Dispose()
    $brush.Dispose()
    $whiteBrush.Dispose()
    $itemFill.Dispose()
    $accentFill.Dispose()
    $titleFont.Dispose()
    $rootFont.Dispose()
    $itemFont.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

$docSections = @(
    @{
        Heading = '1. 志愿者子功能模块设计'
        Summary = '志愿者作为系统中交互最为高频的一类用户，所涉操作大致围绕一次完整参与闭环展开，主要包括账号注册登录、个人资料维护、活动浏览检索、活动报名与取消、现场签到签退、志愿时长查询以及活动评价提交。本研究在切分该子模块时，并未刻意追求功能数量上的对等，而是把贴近使用习惯的入口前置，让志愿者进入系统之后能够以较短路径触达核心动作；与之相关的查询、修改类能力则收敛到资料维护与时长查询两个稳态入口，避免功能堆叠对认知造成额外负担。从实际应用看，这种组织方式更贴近普通用户的使用节奏，仍存在一定优化空间，后续可结合用户行为数据继续迭代。'
        FigureTitle = '图1 志愿者子功能模块图'
        ImageName = 'volunteer-module.png'
        Accent = [System.Drawing.Color]::FromArgb(67, 116, 214)
        RootLabel = '志愿者'
        Items = @('注册登录', '个人资料维护', '活动浏览检索', '活动报名与取消', '签到签退', '时长查询与活动评价')
    },
    @{
        Heading = '2. 组织者子功能模块设计'
        Summary = '组织者偏向活动供给侧，其职责更聚焦于一次活动从筹办到收尾的全过程。结合系统实现可以看到，该角色具备的能力涵盖活动创建与发布、活动信息维护、封面图上传、我的活动列表管理、报名材料审核、签到记录查看以及志愿时长审核。本研究认为，组织者并不直接承担平台级用户治理职责，其权限边界严格落在自己发布的活动之内，这与代码中权限拦截逻辑保持一致。从实际应用看，组织者的多数操作链路并非线性：开启活动后会反复回到列表页处理报名、跟踪签到、复核时长。子模块在切分时也照顾到了这一节奏，使活动维护与审核类入口能够并行使用。'
        FigureTitle = '图2 组织者子功能模块图'
        ImageName = 'organizer-module.png'
        Accent = [System.Drawing.Color]::FromArgb(35, 153, 111)
        RootLabel = '组织者'
        Items = @('活动创建与发布', '活动信息维护', '封面图上传', '我的活动管理', '报名材料审核', '签到与时长审核')
    },
    @{
        Heading = '3. 管理员子功能模块设计'
        Summary = '管理员承担系统的整体治理职责，其功能范围覆盖用户的增删改查、活动审核、活动信息更新、活动删除、平台级评价与异常内容处理。因角色判定通过统一的权限拦截完成，其入口与志愿者、组织者完全分离，本研究在子模块划分时也将其独立呈现。仍需指出的是，管理员的功能并不是在普通用户基础上叠加得到的，而是按治理目标重新组织——例如对用户的删除与对活动的删除分属两个不同语义的操作，前者关乎账号生命周期，后者关乎内容治理；将二者并列收口在同一子模块下，是为了把平台兜底能力集中在一个角色身上，避免权限分散造成的越权风险。'
        FigureTitle = '图3 管理员子功能模块图'
        ImageName = 'admin-module.png'
        Accent = [System.Drawing.Color]::FromArgb(208, 90, 72)
        RootLabel = '管理员'
        Items = @('用户管理', '活动审核', '活动信息更新', '活动删除与状态干预', '志愿时长复核', '评价与异常内容处置')
    }
)

foreach ($section in $docSections) {
    $imagePath = Join-Path $mediaDir $section.ImageName
    Draw-ModuleDiagram -OutputPath $imagePath -Title $section.RootLabel -RootLabel $section.RootLabel -Items $section.Items -AccentColor $section.Accent
}

$bodyXmlParts = New-Object System.Collections.Generic.List[string]
$bodyXmlParts.Add((Add-ParagraphXml -Text '系统功能设计' -FontSize 36 -Justification 'center' -Bold $true -SpacingBefore 120 -SpacingAfter 240))
$bodyXmlParts.Add((Add-ParagraphXml -Text '本章按照志愿者、组织者、管理员三类角色分别说明系统的功能边界。每一类子模块先用一段文字概述其职责覆盖范围，再结合模块图呈现内部结构，把抽象的角色定位落到具体的页面入口与操作链路中，便于后续在模块开发与权限校验环节一一对照。' -FontSize 24 -Justification 'both' -SpacingBefore 0 -SpacingAfter 200 -FirstLineIndent 2))

$docPrId = 1
$relId = 1
$rels = New-Object System.Collections.Generic.List[string]
$contentTypeExtra = '<Default Extension="png" ContentType="image/png"/>'

foreach ($section in $docSections) {
    $bodyXmlParts.Add((Add-ParagraphXml -Text $section.Heading -FontSize 28 -Justification 'left' -Bold $true -SpacingBefore 200 -SpacingAfter 160))
    $bodyXmlParts.Add((Add-ParagraphXml -Text $section.Summary -FontSize 24 -Justification 'both' -SpacingBefore 0 -SpacingAfter 160 -FirstLineIndent 2))
    $bodyXmlParts.Add((Add-ImageParagraphXml -RelationshipId "rId$relId" -DocPrId $docPrId -Name $section.ImageName -Cx 5486400 -Cy 3771900))
    $bodyXmlParts.Add((Add-ParagraphXml -Text $section.FigureTitle -FontSize 22 -Justification 'center' -SpacingBefore 0 -SpacingAfter 200))
    $rels.Add("<Relationship Id='rId$relId' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/image' Target='media/$($section.ImageName)'/>")
    $docPrId++
    $relId++
}

$documentXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk" xmlns:wne="http://schemas.microsoft.com/office/2006/relationships" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" mc:Ignorable="w14 wp14">
  <w:body>
    $($bodyXmlParts -join "`r`n    ")
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1440" w:right="1800" w:bottom="1440" w:left="1800" w:header="851" w:footer="992" w:gutter="0"/>
      <w:cols w:space="425"/>
      <w:docGrid w:linePitch="312"/>
    </w:sectPr>
  </w:body>
</w:document>
"@

$documentRelsXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  $($rels -join "`r`n  ")
</Relationships>
"@

$contentTypesXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  $contentTypeExtra
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
"@

$pkgRelsXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
"@

$coreXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>系统功能设计</dc:title>
  <dc:creator>li</dc:creator>
  <cp:lastModifiedBy>li</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">2026-05-08T00:00:00Z</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">2026-05-08T00:00:00Z</dcterms:modified>
</cp:coreProperties>
"@

$appXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office Word</Application>
</Properties>
"@

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText((Join-Path $buildDir 'word\document.xml'), $documentXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $buildDir 'word\_rels\document.xml.rels'), $documentRelsXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $buildDir '[Content_Types].xml'), $contentTypesXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $buildDir '_rels\.rels'), $pkgRelsXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $buildDir 'docProps\core.xml'), $coreXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $buildDir 'docProps\app.xml'), $appXml, $utf8NoBom)

$zipPath = Join-Path $root 'tmp_docx_build\系统功能设计.zip'
$docxPath = Join-Path $root '系统功能设计.docx'

if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
if (Test-Path $docxPath) {
    Remove-Item -LiteralPath $docxPath -Force
}

Compress-Archive -Path (Join-Path $buildDir '*') -DestinationPath $zipPath -Force
Move-Item -LiteralPath $zipPath -Destination $docxPath -Force

Write-Output $docxPath
