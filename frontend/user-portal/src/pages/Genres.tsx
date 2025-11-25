import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { X, Plus, Search, Loader2 } from "lucide-react";
import Header from "@/components/Header";
import { toast } from "sonner";
import { genreApi, type Genre } from "@/lib/genreApi";
import { useAuth } from "@/hooks/useAuth";

const suggestedGenres = [
  "Fiction",
  "Poetry",
  "Biography",
  "Children's Books",
  "Mystery & Thriller",
  "Science Fiction",
  "Fantasy",
  "Romance",
  "Historical Fiction",
  "Self-Help",
  "Business",
  "Philosophy",
  "Travel",
  "Cookbooks",
  "Art & Photography",
  "Religion & Spirituality",
  "Science & Nature",
  "Comics & Graphic Novels",
  "Young Adult",
  "Literary Criticism",
];

export default function Genres() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const [searchQuery, setSearchQuery] = useState("");
  const [customGenre, setCustomGenre] = useState("");
  const [selectedGenres, setSelectedGenres] = useState<Genre[]>([]);
  const [removingId, setRemovingId] = useState<string | null>(null);

  const genresQuery = useQuery({
    queryKey: ["genres", user?.id],
    queryFn: () => genreApi.listByUser(user?.id || ""),
    enabled: !!user?.id,
  });

  useEffect(() => {
    if (genresQuery.data) {
      setSelectedGenres(genresQuery.data);
    }
  }, [genresQuery.data]);

  const addGenreMutation = useMutation({
    mutationFn: (name: string) =>
      genreApi.create({
        name,
        userId: user?.id || "",
        displayOrder: selectedGenres.length,
      }),
    onSuccess: (genre) => {
      setSelectedGenres((prev) => [...prev, genre]);
      setCustomGenre("");
      toast.success(`${genre.name} added`);
      queryClient.invalidateQueries({ queryKey: ["genres", user?.id] });
    },
    onError: (err) => {
      toast.error((err as Error).message || "Could not add genre");
    },
  });

  const removeGenreMutation = useMutation({
    mutationFn: (genreId: string) => genreApi.remove(genreId),
    onMutate: (genreId) => setRemovingId(genreId),
    onSuccess: (_, genreId) => {
      setSelectedGenres((prev) => prev.filter((g) => g.id !== genreId));
      toast.success("Genre removed");
      queryClient.invalidateQueries({ queryKey: ["genres", user?.id] });
    },
    onError: (err) => toast.error((err as Error).message || "Could not remove genre"),
    onSettled: () => setRemovingId(null),
  });

  const filteredSuggestions = useMemo(() => {
    return suggestedGenres.filter(
      (genre) =>
        genre.toLowerCase().includes(searchQuery.toLowerCase()) &&
        !selectedGenres.some((g) => g.name.toLowerCase() === genre.toLowerCase()),
    );
  }, [searchQuery, selectedGenres]);

  const handleAddGenre = (name: string) => {
    const trimmed = name.trim();
    if (!trimmed) {
      toast.error("Please enter a genre name");
      return;
    }
    if (!user?.id) {
      toast.error("You need to be signed in to add genres");
      return;
    }
    if (selectedGenres.some((g) => g.name.toLowerCase() === trimmed.toLowerCase())) {
      toast.error("Genre already added");
      return;
    }
    addGenreMutation.mutate(trimmed);
  };

  const handleRemoveGenre = (genre: Genre) => {
    if (!genre.id) return;
    removeGenreMutation.mutate(genre.id);
  };

  const handleSave = () => {
    if (selectedGenres.length === 0) {
      toast.error("Please select at least one genre");
      return;
    }
    toast.success("Genres saved");
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
              {genresQuery.isLoading ? (
                <p className="text-sm text-muted-foreground">Loading your genres...</p>
              ) : selectedGenres.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">
                  No genres selected yet. Choose from the list below or add custom genres.
                </p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {selectedGenres.map((genre) => (
                    <Badge key={genre.id ?? genre.name} variant="secondary" className="text-sm px-3 py-1">
                      {genre.name}
                      <button
                        onClick={() => handleRemoveGenre(genre)}
                        className="ml-2 hover:text-destructive"
                        disabled={removeGenreMutation.isPending && removingId === genre.id}
                      >
                        {removeGenreMutation.isPending && removingId === genre.id ? (
                          <Loader2 className="h-3 w-3 animate-spin" />
                        ) : (
                          <X className="h-3 w-3" />
                        )}
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
                  onKeyDown={(e) => e.key === "Enter" && handleAddGenre(customGenre)}
                />
                <Button onClick={() => handleAddGenre(customGenre)} variant="secondary">
                  {addGenreMutation.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Plus className="h-4 w-4" />
                  )}
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
                {filteredSuggestions.map((genre) => (
                  <Badge
                    key={genre}
                    variant="outline"
                    className="text-sm px-3 py-2 cursor-pointer hover:bg-primary hover:text-primary-foreground transition-colors"
                    onClick={() => handleAddGenre(genre)}
                  >
                    <Plus className="h-3 w-3 mr-1" />
                    {genre}
                  </Badge>
                ))}
                {filteredSuggestions.length === 0 && searchQuery && (
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
