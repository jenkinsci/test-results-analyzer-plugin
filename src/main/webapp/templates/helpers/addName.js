export default function (name) {
    var modName = "";
    modName = name.replace(/[^a-z\d/-]+/gi, "_");
    return modName;
}
