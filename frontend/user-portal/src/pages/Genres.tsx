import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { X, Plus, Search } from "lucide-react";
import Header from "@/components/Header";
import { availableLiteraryGenres } from "@/data/mockData";
import { toast } from "sonner";

export default function Genres() {
  const navigate = useNavigate();
  const [selectedGenres, setSelectedGenres] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [customGenre, setCustomGenre] = useState("");

  const filteredGenres = availableLiteraryGenres.filter(genre =>
    genre.toLowerCase().includes(searchQuery.toLowerCase()) &&
    !selectedGenres.includes(genre)
  );

  const toggleGenre = (genre: string) => {
    if (selectedGenres.includes(genre)) {
      setSelectedGenres(selectedGenres.filter(g => g !== genre));
    } else {
      setSelectedGenres([...selectedGenres, genre]);
    }
  };

  const addCustomGenre = () => {
    if (!customGenre.trim()) return;
    
    if (selectedGenres.includes(customGenre.trim())) {
      toast.error("Genre already added");
      return;
    }
    
    setSelectedGenres([...selectedGenres, customGenre.trim()]);
    setCustomGenre("");
    toast.success("Custom genre added");
  };

  const handleSave = () => {
    if (selectedGenres.length === 0) {
      toast.error("Please select at least one genre");
      return;
    }
    
    toast.success(`${selectedGenres.length} genres saved successfully!`);
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-4xl mx-auto space-y-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">Literary Genres</h1>
            <p className="text-muted-foreground">
              Select the genres you publish to help visitors discover your stall
            </p>
          </div>

          {/* Selected Genres */}
          <Card>
            <CardHeader>
              <CardTitle>Selected Genres ({selectedGenres.length})</CardTitle>
              <CardDescription>These will be displayed on your stall profile</CardDescription>
            </CardHeader>
            <CardContent>
              {selectedGenres.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">
                  No genres selected yet. Choose from the list below or add custom genres.
                </p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {selectedGenres.map((genre) => (
                    <Badge key={genre} variant="secondary" className="text-sm px-3 py-1">
                      {genre}
                      <button
                        onClick={() => toggleGenre(genre)}
                        className="ml-2 hover:text-destructive"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Search and Custom Genre */}
          <Card>
            <CardHeader>
              <CardTitle>Add Genres</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search genres..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9"
                />
              </div>

              <div className="flex gap-2">
                <Input
                  placeholder="Add custom genre..."
                  value={customGenre}
                  onChange={(e) => setCustomGenre(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && addCustomGenre()}
                />
                <Button onClick={addCustomGenre} variant="secondary">
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Available Genres */}
          <Card>
            <CardHeader>
              <CardTitle>Available Genres</CardTitle>
              <CardDescription>Click to add to your selection</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {filteredGenres.map((genre) => (
                  <Badge
                    key={genre}
                    variant="outline"
                    className="text-sm px-3 py-2 cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors"
                    onClick={() => toggleGenre(genre)}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    {genre}
                  </Badge>
                ))}
                {filteredGenres.length === 0 && searchQuery && (
                  <p className="text-sm text-muted-foreground">
                    No genres found. Try adding a custom genre above.
                  </p>
                )}
              </div>
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button variant="outline" className="flex-1" onClick={() => navigate("/dashboard")}>
              Cancel
            </Button>
            <Button variant="hero" className="flex-1" onClick={handleSave}>
              Save & Continue
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
