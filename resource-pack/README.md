# ArcaneCore Resource Pack

This folder is the editable resource pack for ArcaneCore.

Correct pack layout:

```text
resource-pack/
  pack.mcmeta
  assets/
```

When you zip the pack, zip the contents of this folder, not the folder itself.

From the ArcaneCore workspace:

```powershell
Compress-Archive -Path .\resource-pack\pack.mcmeta, .\resource-pack\assets -DestinationPath .\ArcaneCoreResourcePack.zip -Force
```

The status font is here:

```text
assets/arcanesmp/font/status.json
```

The status font textures are here:

```text
assets/arcanesmp/textures/font/status/
```

Glyphs currently mapped:

```text
\uE101 = Ember
\uE102 = Frost
\uE103 = Breeze
\uE104 = Luck
\uE105 = Void
```

Plugin config for using these icons:

```yml
status-bar:
  resource-pack-icons: true
  resource-pack-font: arcanesmp:status
```

If the pack is not installed on the client, set `resource-pack-icons: false`
so players see the normal emoji/text fallback instead of missing glyph boxes.
