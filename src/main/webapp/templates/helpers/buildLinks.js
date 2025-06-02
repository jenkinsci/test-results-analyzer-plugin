export default function (object) {
    return new Handlebars.SafeString(object.slice(0, 10).map(function( value, index ) {
        var url = Handlebars.escapeExpression(value.buildUrl),
        text = Handlebars.escapeExpression(value.buildNumber);
        return "<a href=" + url + ">" + text + "</a>";
    }).join(', '));
};
