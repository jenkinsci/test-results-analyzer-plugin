export default function (hierarchyLevel) {
    var parent = "|"
    var ident = "-";
    for(var i =0;i<hierarchyLevel;i++){
        ident = ident + ident;
    }

    return new Handlebars.SafeString(parent+ident);
}
