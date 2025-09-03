export default function (context, parentHierarchy, options) {
    if (parentHierarchy == undefined)
    parentHierarchy = 0;
    context["hierarchyLevel"] = parentHierarchy + 1;
}
