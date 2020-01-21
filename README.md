### CircleExpandableView

`CircleExpandableView` is my attempt to get acquainted with custom view drawing in Android

#### Things it can do:
- Draw N icons at the vertices of N-polygon
- N can be set by either `setNodeCount` method or xml attribute `cev_nodeCount`
- Expand and collapse with animation. When collapsed it is drawn as one icon in the center of N-polygon. It can be made by using `expand` and `collapse` methods
- Selected icon index can be set by `setSelectedIndex` method. Selected icon is drawn black
- Rotate can be made either by user touch or by using `rotateClockwise` and `rotateCounterClockwise` methods

#### Things TODO:
- Clean code
- Add ability for user to set rotation angle
- Perform click on user touch
- Add minimum distance before view can start rotating by touch